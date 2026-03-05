#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
MXW01 Printer - Kivy Mobile App
Android native Bluetooth kullanır
"""

from kivy.app import App
from kivy.uix.screenmanager import ScreenManager, Screen
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput
from kivy.uix.image import Image as KivyImage
from kivy.uix.popup import Popup
from kivy.uix.scrollview import ScrollView
from kivy.uix.gridlayout import GridLayout
from kivy.core.window import Window
from kivy.clock import Clock
from kivy.metrics import dp
from kivy.core.image import Image as CoreImage

import io
from PIL import Image, ImageDraw, ImageFont

try:
    from jnius import autoclass
    BluetoothAdapter = autoclass('android.bluetooth.BluetoothAdapter')
    BluetoothDevice = autoclass('android.bluetooth.BluetoothDevice')
    BluetoothSocket = autoclass('android.bluetooth.BluetoothSocket')
    UUID = autoclass('java.util.UUID')
    ANDROID = True
except:
    ANDROID = False

Window.size = (360, 640)

class PrinterProtocol:
    CMD_START = bytes.fromhex("2221A70000000000")
    CMD_CONFIG1 = bytes.fromhex("2221B10001000000FF")
    CMD_CONFIG2 = bytes.fromhex("2221A10001000000FF")
    CMD_HEAT = bytes.fromhex("2221A2000100FFFFFF")
    CMD_HEADER = bytes.fromhex("2221A9000400000230000000")
    CMD_END = bytes.fromhex("2221AD000100000000")
    
    @staticmethod
    def encode_lsb(img: Image.Image, width: int = 384) -> bytes:
        bw = img.convert('L').point(lambda p: 0 if p < 128 else 255, '1')
        bytes_per_row = width // 8
        data = []
        
        for y in range(bw.height):
            for x in range(0, width, 8):
                byte = 0
                for bit in range(8):
                    if x + bit < bw.width and bw.getpixel((x + bit, y)) == 0:
                        byte |= (1 << bit)
                data.append(byte)
        
        return bytes(data)
    
    @staticmethod
    def text_to_image(text: str, width: int = 384, font_size: int = 60) -> Image.Image:
        lines = text.split('\n') if text else ["Preview"]
        
        try:
            font = ImageFont.truetype("/system/fonts/DroidSans.ttf", font_size)
        except:
            font = ImageFont.load_default()
        
        line_height = font_size + 8
        total_height = len(lines) * line_height + 24
        
        img = Image.new('RGB', (width, total_height), 'white')
        draw = ImageDraw.Draw(img)
        
        y = 12
        for line in lines:
            if line.strip():
                bbox = draw.textbbox((0, 0), line, font=font)
                text_width = bbox[2] - bbox[0]
                x = (width - text_width) // 2
                draw.text((x, y), line, fill='black', font=font)
            y += line_height
        
        return img

class HomeScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        
        layout = BoxLayout(orientation='vertical', padding=dp(20), spacing=dp(15))
        
        title = Label(text='MXW01 Yazıcı', font_size='24sp', size_hint_y=0.1, bold=True)
        layout.add_widget(title)
        
        self.status_label = Label(text='Bağlı Değil', font_size='14sp', size_hint_y=0.05)
        layout.add_widget(self.status_label)
        
        conn_box = BoxLayout(orientation='vertical', size_hint_y=0.15, spacing=dp(5))
        self.printer_name = Label(text='MXW01', font_size='16sp')
        self.printer_mac = Label(text='48:0F:57:3E:60:77', font_size='12sp')
        conn_box.add_widget(self.printer_name)
        conn_box.add_widget(self.printer_mac)
        
        connect_btn = Button(text='Yazıcı Bağla', size_hint_y=None, height=dp(50))
        connect_btn.bind(on_press=lambda x: setattr(self.manager, 'current', 'bluetooth'))
        conn_box.add_widget(connect_btn)
        layout.add_widget(conn_box)
        
        menu = GridLayout(cols=2, spacing=dp(15), size_hint_y=0.5)
        
        text_btn = Button(text='📝\nMetin', font_size='16sp')
        text_btn.bind(on_press=lambda x: setattr(self.manager, 'current', 'text'))
        menu.add_widget(text_btn)
        
        settings_btn = Button(text='⚙️\nAyarlar', font_size='16sp')
        settings_btn.bind(on_press=lambda x: setattr(self.manager, 'current', 'settings'))
        menu.add_widget(settings_btn)
        
        layout.add_widget(menu)
        
        footer = Label(text='v1.0 - Android Native', size_hint_y=0.05, font_size='12sp')
        layout.add_widget(footer)
        
        self.add_widget(layout)

class TextScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        
        layout = BoxLayout(orientation='vertical', padding=dp(20), spacing=dp(15))
        
        header = BoxLayout(size_hint_y=0.08)
        back_btn = Button(text='← Geri', size_hint_x=0.3)
        back_btn.bind(on_press=lambda x: setattr(self.manager, 'current', 'home'))
        header.add_widget(back_btn)
        header.add_widget(Label(text='Metin Yazdır', font_size='18sp'))
        layout.add_widget(header)
        
        self.text_input = TextInput(hint_text='Metninizi yazın...', multiline=True, size_hint_y=0.4)
        layout.add_widget(self.text_input)
        
        self.preview_image = KivyImage(size_hint_y=0.4)
        layout.add_widget(self.preview_image)
        
        btn_box = BoxLayout(size_hint_y=0.12, spacing=dp(10))
        
        preview_btn = Button(text='Önizleme')
        preview_btn.bind(on_press=self.update_preview)
        
        print_btn = Button(text='Yazdır')
        print_btn.bind(on_press=self.print_text)
        
        btn_box.add_widget(preview_btn)
        btn_box.add_widget(print_btn)
        layout.add_widget(btn_box)
        
        self.add_widget(layout)
        self.current_image = None
    
    def update_preview(self, instance):
        text = self.text_input.text.strip() or "Önizleme"
        
        try:
            img = PrinterProtocol.text_to_image(text)
            self.current_image = img
            
            buf = io.BytesIO()
            img.save(buf, format='PNG')
            buf.seek(0)
            core_img = CoreImage(buf, ext='png')
            self.preview_image.texture = core_img.texture
        except Exception as e:
            print(f"Preview error: {e}")
    
    def print_text(self, instance):
        if not self.current_image:
            self.update_preview(instance)
        
        if self.current_image:
            app = App.get_running_app()
            app.start_print(self.current_image)

class BluetoothScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        
        layout = BoxLayout(orientation='vertical', padding=dp(20), spacing=dp(15))
        
        header = BoxLayout(size_hint_y=0.08)
        back_btn = Button(text='← Geri', size_hint_x=0.3)
        back_btn.bind(on_press=lambda x: setattr(self.manager, 'current', 'home'))
        header.add_widget(back_btn)
        header.add_widget(Label(text='Bluetooth', font_size='18sp'))
        layout.add_widget(header)
        
        self.status_label = Label(text='Taramaya hazır', size_hint_y=0.08)
        layout.add_widget(self.status_label)
        
        scroll = ScrollView(size_hint_y=0.6)
        self.device_list = GridLayout(cols=1, spacing=dp(10), size_hint_y=None)
        self.device_list.bind(minimum_height=self.device_list.setter('height'))
        scroll.add_widget(self.device_list)
        layout.add_widget(scroll)
        
        self.scan_btn = Button(text='Cihazları Tara', size_hint_y=0.12)
        self.scan_btn.bind(on_press=self.start_scan)
        layout.add_widget(self.scan_btn)
        
        self.add_widget(layout)
    
    def start_scan(self, instance):
        if not ANDROID:
            self.status_label.text = 'Android gerekli'
            return
        
        try:
            adapter = BluetoothAdapter.getDefaultAdapter()
            if not adapter:
                self.status_label.text = 'Bluetooth yok'
                return
            
            devices = adapter.getBondedDevices().toArray()
            self.device_list.clear_widgets()
            
            if not devices:
                self.status_label.text = 'Eşleşmiş cihaz yok'
                return
            
            self.status_label.text = f'{len(devices)} cihaz bulundu'
            
            for device in devices:
                name = device.getName()
                address = device.getAddress()
                btn = Button(text=f'{name}\n{address}', size_hint_y=None, height=dp(70))
                btn.bind(on_press=lambda x, n=name, a=address: self.select_device(n, a))
                self.device_list.add_widget(btn)
        except Exception as e:
            self.status_label.text = f'Hata: {e}'
    
    def select_device(self, name, address):
        app = App.get_running_app()
        app.printer_name = name
        app.printer_address = address
        
        home = self.manager.get_screen('home')
        home.printer_name.text = name
        home.printer_mac.text = address
        home.status_label.text = 'Bağlandı'
        
        self.manager.current = 'home'

class SettingsScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        
        layout = BoxLayout(orientation='vertical', padding=dp(20), spacing=dp(15))
        
        header = BoxLayout(size_hint_y=0.08)
        back_btn = Button(text='← Geri', size_hint_x=0.3)
        back_btn.bind(on_press=lambda x: setattr(self.manager, 'current', 'home'))
        header.add_widget(back_btn)
        header.add_widget(Label(text='Ayarlar', font_size='18sp'))
        layout.add_widget(header)
        
        info = Label(text='MXW01 Printer v1.0\nAndroid Native Bluetooth', size_hint_y=0.8)
        layout.add_widget(info)
        
        self.add_widget(layout)

class MXWPrinterApp(App):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.printer_name = "MXW01"
        self.printer_address = "48:0F:57:3E:60:77"
        self.socket = None
    
    def build(self):
        sm = ScreenManager()
        sm.add_widget(HomeScreen(name='home'))
        sm.add_widget(TextScreen(name='text'))
        sm.add_widget(BluetoothScreen(name='bluetooth'))
        sm.add_widget(SettingsScreen(name='settings'))
        return sm
    
    def start_print(self, image: Image.Image):
        if not ANDROID:
            print("Android gerekli")
            return
        
        try:
            adapter = BluetoothAdapter.getDefaultAdapter()
            device = adapter.getRemoteDevice(self.printer_address)
            
            uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            self.socket = device.createRfcommSocketToServiceRecord(uuid)
            self.socket.connect()
            
            output = self.socket.getOutputStream()
            
            data = PrinterProtocol.encode_lsb(image)
            rows = image.height
            bytes_per_row = 384 // 8
            
            output.write(PrinterProtocol.CMD_START)
            output.flush()
            
            output.write(PrinterProtocol.CMD_CONFIG1)
            output.flush()
            
            output.write(PrinterProtocol.CMD_CONFIG2)
            output.flush()
            
            output.write(PrinterProtocol.CMD_HEAT)
            output.flush()
            
            output.write(PrinterProtocol.CMD_HEADER)
            output.flush()
            
            for i in range(rows):
                row = data[i * bytes_per_row:(i + 1) * bytes_per_row]
                output.write(row)
                output.flush()
            
            output.write(PrinterProtocol.CMD_END)
            output.flush()
            
            self.socket.close()
            print("Yazdırma tamamlandı!")
            
        except Exception as e:
            print(f"Print error: {e}")
            if self.socket:
                self.socket.close()

if __name__ == '__main__':
    MXWPrinterApp().run()
