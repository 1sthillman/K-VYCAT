#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
MXW01 Printer - Kivy Mobile App
Python kodundaki AYNI protokol
"""

from kivy.app import App
from kivy.uix.screenmanager import ScreenManager, Screen
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput
from kivy.uix.image import Image as KivyImage
from kivy.uix.slider import Slider
from kivy.uix.spinner import Spinner
from kivy.uix.popup import Popup
from kivy.uix.scrollview import ScrollView
from kivy.uix.gridlayout import GridLayout
from kivy.core.window import Window
from kivy.clock import Clock
from kivy.metrics import dp
from kivy.core.image import Image as CoreImage

import asyncio
import io
from PIL import Image, ImageDraw, ImageFont, ImageEnhance
from enum import Enum

try:
    from bleak import BleakClient, BleakScanner
    BLE_AVAILABLE = True
except:
    BLE_AVAILABLE = False

try:
    import qrcode
    HAS_QR = True
except:
    HAS_QR = False

Window.size = (360, 640)

# Printer Protocol - Python kodundan AYNI
class PrinterProtocol:
    CMD_UUID = "0000ae01-0000-1000-8000-00805f9b34fb"
    DATA_UUID = "0000ae03-0000-1000-8000-00805f9b34fb"
    
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
        
        # Header
        title = Label(text='MXW01 Yazıcı', font_size='24sp', size_hint_y=0.1, bold=True)
        layout.add_widget(title)
        
        self.status_label = Label(text='Bağlı Değil', font_size='14sp', size_hint_y=0.05)
        layout.add_widget(self.status_label)
        
        # Connection
        conn_box = BoxLayout(orientation='vertical', size_hint_y=0.15, spacing=dp(5))
        self.printer_name = Label(text='MXW01', font_size='16sp')
        self.printer_mac = Label(text='48:0F:57:3E:60:77', font_size='12sp')
        conn_box.add_widget(self.printer_name)
        conn_box.add_widget(self.printer_mac)
        
        connect_btn = Button(text='Yazıcı Bağla', size_hint_y=None, height=dp(50))
        connect_btn.bind(on_press=lambda x: setattr(self.manager, 'current', 'bluetooth'))
        conn_box.add_widget(connect_btn)
        layout.add_widget(conn_box)
        
        # Menu
        menu = GridLayout(cols=2, spacing=dp(15), size_hint_y=0.5)
        
        text_btn = Button(text='📝\nMetin', font_size='16sp')
        text_btn.bind(on_press=lambda x: setattr(self.manager, 'current', 'text'))
        
        image_btn = Button(text='🖼️\nGörsel', font_size='16sp')
        image_btn.bind(on_press=lambda x: setattr(self.manager, 'current', 'image'))
        
        qr_btn = Button(text='📱\nQR Kod', font_size='16sp')
        qr_btn.bind(on_press=lambda x: setattr(self.manager, 'current', 'qr'))
        
        settings_btn = Button(text='⚙️\nAyarlar', font_size='16sp')
        settings_btn.bind(on_press=lambda x: setattr(self.manager, 'current', 'settings'))
        
        menu.add_widget(text_btn)
        menu.add_widget(image_btn)
        menu.add_widget(qr_btn)
        menu.add_widget(settings_btn)
        
        layout.add_widget(menu)
        
        footer = Label(text='v1.0 - Kivy', size_hint_y=0.05, font_size='12sp')
        layout.add_widget(footer)
        
        self.add_widget(layout)

class TextScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        
        layout = BoxLayout(orientation='vertical', padding=dp(20), spacing=dp(15))
        
        # Header
        header = BoxLayout(size_hint_y=0.08)
        back_btn = Button(text='← Geri', size_hint_x=0.3)
        back_btn.bind(on_press=lambda x: setattr(self.manager, 'current', 'home'))
        header.add_widget(back_btn)
        header.add_widget(Label(text='Metin Yazdır', font_size='18sp'))
        layout.add_widget(header)
        
        # Input
        self.text_input = TextInput(hint_text='Metninizi yazın...', multiline=True, size_hint_y=0.4)
        layout.add_widget(self.text_input)
        
        # Preview
        self.preview_image = KivyImage(size_hint_y=0.4)
        layout.add_widget(self.preview_image)
        
        # Buttons
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
        self.devices = []
    
    def start_scan(self, instance):
        self.scan_btn.disabled = True
        self.scan_btn.text = 'Taranıyor...'
        self.status_label.text = 'Bluetooth cihazları aranıyor...'
        self.device_list.clear_widgets()
        
        Clock.schedule_once(lambda dt: self.scan_devices(), 0.1)
    
    def scan_devices(self):
        try:
            loop = asyncio.new_event_loop()
            asyncio.set_event_loop(loop)
            devices = loop.run_until_complete(BleakScanner.discover(timeout=5.0))
            loop.close()
            
            self.devices = [(d.name or "Unknown", d.address) for d in devices]
            self.display_devices()
        except Exception as e:
            self.status_label.text = f'Hata: {e}'
            self.scan_btn.disabled = False
            self.scan_btn.text = 'Tekrar Tara'
    
    def display_devices(self):
        self.device_list.clear_widgets()
        
        if not self.devices:
            self.status_label.text = 'Cihaz bulunamadı'
        else:
            self.status_label.text = f'{len(self.devices)} cihaz bulundu'
            
            for name, address in self.devices:
                btn = Button(text=f'{name}\n{address}', size_hint_y=None, height=dp(70))
                btn.bind(on_press=lambda x, n=name, a=address: self.select_device(n, a))
                self.device_list.add_widget(btn)
        
        self.scan_btn.disabled = False
        self.scan_btn.text = 'Tekrar Tara'
    
    def select_device(self, name, address):
        app = App.get_running_app()
        app.printer_name = name
        app.printer_address = address
        
        home = self.manager.get_screen('home')
        home.printer_name.text = name
        home.printer_mac.text = address
        home.status_label.text = 'Bağlandı'
        
        self.manager.current = 'home'

class QRScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        
        layout = BoxLayout(orientation='vertical', padding=dp(20), spacing=dp(15))
        
        header = BoxLayout(size_hint_y=0.08)
        back_btn = Button(text='← Geri', size_hint_x=0.3)
        back_btn.bind(on_press=lambda x: setattr(self.manager, 'current', 'home'))
        header.add_widget(back_btn)
        header.add_widget(Label(text='QR Kod', font_size='18sp'))
        layout.add_widget(header)
        
        self.qr_input = TextInput(hint_text='https://ornek.com', multiline=False, size_hint_y=0.1)
        layout.add_widget(self.qr_input)
        
        self.preview_image = KivyImage(size_hint_y=0.5)
        layout.add_widget(self.preview_image)
        
        btn_box = BoxLayout(size_hint_y=0.12, spacing=dp(10))
        
        preview_btn = Button(text='Önizleme')
        preview_btn.bind(on_press=self.generate_preview)
        
        print_btn = Button(text='Yazdır')
        print_btn.bind(on_press=self.print_qr)
        
        btn_box.add_widget(preview_btn)
        btn_box.add_widget(print_btn)
        layout.add_widget(btn_box)
        
        self.add_widget(layout)
        self.current_qr = None
    
    def generate_preview(self, instance):
        if not HAS_QR:
            return
        
        data = self.qr_input.text.strip()
        if not data:
            return
        
        try:
            qr = qrcode.QRCode(box_size=10, border=4)
            qr.add_data(data)
            qr.make(fit=True)
            img = qr.make_image(fill_color="black", back_color="white")
            
            self.current_qr = img.convert('RGB')
            
            buf = io.BytesIO()
            self.current_qr.save(buf, format='PNG')
            buf.seek(0)
            core_img = CoreImage(buf, ext='png')
            self.preview_image.texture = core_img.texture
        except Exception as e:
            print(f"QR error: {e}")
    
    def print_qr(self, instance):
        if not self.current_qr:
            self.generate_preview(instance)
        
        if self.current_qr:
            canvas = Image.new('RGB', (384, self.current_qr.height + 20), 'white')
            x = (384 - self.current_qr.width) // 2
            canvas.paste(self.current_qr, (x, 10))
            
            app = App.get_running_app()
            app.start_print(canvas)

class ImageScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        
        layout = BoxLayout(orientation='vertical', padding=dp(20), spacing=dp(15))
        
        header = BoxLayout(size_hint_y=0.08)
        back_btn = Button(text='← Geri', size_hint_x=0.3)
        back_btn.bind(on_press=lambda x: setattr(self.manager, 'current', 'home'))
        header.add_widget(back_btn)
        header.add_widget(Label(text='Görsel', font_size='18sp'))
        layout.add_widget(header)
        
        load_btn = Button(text='Görsel Yükle', size_hint_y=0.08)
        layout.add_widget(load_btn)
        
        self.preview_image = KivyImage(size_hint_y=0.6)
        layout.add_widget(self.preview_image)
        
        print_btn = Button(text='Yazdır', size_hint_y=0.12)
        layout.add_widget(print_btn)
        
        self.add_widget(layout)

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
        
        info = Label(text='MXW01 Printer v1.0\nKivy ile geliştirildi', size_hint_y=0.8)
        layout.add_widget(info)
        
        self.add_widget(layout)

class MXWPrinterApp(App):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.printer_name = "MXW01"
        self.printer_address = "48:0F:57:3E:60:77"
    
    def build(self):
        sm = ScreenManager()
        sm.add_widget(HomeScreen(name='home'))
        sm.add_widget(TextScreen(name='text'))
        sm.add_widget(BluetoothScreen(name='bluetooth'))
        sm.add_widget(QRScreen(name='qr'))
        sm.add_widget(ImageScreen(name='image'))
        sm.add_widget(SettingsScreen(name='settings'))
        return sm
    
    def start_print(self, image: Image.Image):
        if not BLE_AVAILABLE:
            return
        
        Clock.schedule_once(lambda dt: self.print_async(image), 0.1)
    
    def print_async(self, image):
        try:
            loop = asyncio.new_event_loop()
            asyncio.set_event_loop(loop)
            loop.run_until_complete(self.print_image(image))
            loop.close()
        except Exception as e:
            print(f"Print error: {e}")
    
    async def print_image(self, image: Image.Image):
        device = await BleakScanner.find_device_by_address(self.printer_address, timeout=10.0)
        if not device:
            return
        
        async with BleakClient(device) as client:
            data = PrinterProtocol.encode_lsb(image)
            rows = image.height
            bytes_per_row = 384 // 8
            
            await client.write_gatt_char(PrinterProtocol.CMD_UUID, PrinterProtocol.CMD_START, response=False)
            await asyncio.sleep(0.5)
            
            await client.write_gatt_char(PrinterProtocol.CMD_UUID, PrinterProtocol.CMD_CONFIG1, response=False)
            await asyncio.sleep(0.5)
            
            await client.write_gatt_char(PrinterProtocol.CMD_UUID, PrinterProtocol.CMD_CONFIG2, response=False)
            await asyncio.sleep(0.5)
            
            await client.write_gatt_char(PrinterProtocol.CMD_UUID, PrinterProtocol.CMD_HEAT, response=False)
            await asyncio.sleep(1.0)
            
            await client.write_gatt_char(PrinterProtocol.CMD_UUID, PrinterProtocol.CMD_HEADER, response=False)
            await asyncio.sleep(1.0)
            
            for i in range(rows):
                row = data[i * bytes_per_row:(i + 1) * bytes_per_row]
                await client.write_gatt_char(PrinterProtocol.DATA_UUID, row, response=False)
                await asyncio.sleep(0.05)
            
            await client.write_gatt_char(PrinterProtocol.CMD_UUID, PrinterProtocol.CMD_END, response=False)
            await asyncio.sleep(2.0)

if __name__ == '__main__':
    MXWPrinterApp().run()
