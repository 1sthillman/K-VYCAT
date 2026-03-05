#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
MXW01 Printer - Kivy Android App
Python printer_app.py'nin mobil versiyonu
"""

import asyncio
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput
from kivy.uix.progressbar import ProgressBar
from kivy.uix.scrollview import ScrollView
from kivy.clock import Clock
from PIL import Image, ImageDraw, ImageFont
import io

try:
    from bleak import BleakClient, BleakScanner
    BLE_AVAILABLE = True
except ImportError:
    BLE_AVAILABLE = False

# Printer Protocol
DEVICE_ADDRESS = "48:0F:57:3E:60:77"
CMD_UUID = "0000ae01-0000-1000-8000-00805f9b34fb"
DATA_UUID = "0000ae03-0000-1000-8000-00805f9b34fb"
PRINTER_WIDTH = 384
BYTES_PER_ROW = 48

def encode_lsb(img: Image.Image) -> bytes:
    """LSB encoding - printer_app.py ile AYNI"""
    data = []
    for y in range(img.height):
        for x in range(0, PRINTER_WIDTH, 8):
            byte = 0
            for bit in range(8):
                if x + bit < img.width and img.getpixel((x + bit, y)) == 0:
                    byte |= (1 << bit)
            data.append(byte)
    return bytes(data)

def text_to_image(text: str, font_size: int = 60) -> Image.Image:
    """Text to bitmap - printer_app.py ile AYNI"""
    lines = text.split('\n') if text.strip() else ["Test"]
    
    # Font
    try:
        font = ImageFont.truetype("/system/fonts/Roboto-Bold.ttf", font_size)
    except:
        try:
            font = ImageFont.truetype("C:/Windows/Fonts/arialbd.ttf", font_size)
        except:
            font = ImageFont.load_default()
    
    # Calculate size
    line_height = font_size + 8
    total_height = len(lines) * line_height + 24
    
    # Create image
    img = Image.new('RGB', (PRINTER_WIDTH, total_height), 'white')
    draw = ImageDraw.Draw(img)
    
    # Draw text
    y = 12
    for line in lines:
        if not line.strip():
            y += line_height
            continue
        
        bbox = draw.textbbox((0, 0), line, font=font)
        text_width = bbox[2] - bbox[0]
        x = (PRINTER_WIDTH - text_width) // 2
        
        draw.text((x, y), line, fill='black', font=font)
        y += line_height
    
    return img.convert('L').point(lambda p: 0 if p < 128 else 255, '1')

class PrinterApp(App):
    def build(self):
        self.title = "MXW01 Printer"
        
        # Main layout
        layout = BoxLayout(orientation='vertical', padding=10, spacing=10)
        
        # Status label
        self.status_label = Label(
            text='Hazır' if BLE_AVAILABLE else 'HATA: Bleak yüklü değil!',
            size_hint=(1, 0.1),
            color=(1, 1, 1, 1)
        )
        layout.add_widget(self.status_label)
        
        # Text input
        self.text_input = TextInput(
            hint_text='Yazdırmak istediğiniz metni girin...',
            multiline=True,
            size_hint=(1, 0.5)
        )
        layout.add_widget(self.text_input)
        
        # Progress bar
        self.progress_bar = ProgressBar(max=100, size_hint=(1, 0.05))
        layout.add_widget(self.progress_bar)
        
        # Log scroll
        log_scroll = ScrollView(size_hint=(1, 0.2))
        self.log_label = Label(
            text='',
            size_hint_y=None,
            markup=True
        )
        self.log_label.bind(texture_size=self.log_label.setter('size'))
        log_scroll.add_widget(self.log_label)
        layout.add_widget(log_scroll)
        
        # Print button
        self.print_btn = Button(
            text='Yazdır',
            size_hint=(1, 0.15),
            background_color=(0.2, 0.6, 1, 1),
            disabled=not BLE_AVAILABLE
        )
        self.print_btn.bind(on_press=self.start_print)
        layout.add_widget(self.print_btn)
        
        self.current_image = None
        
        return layout
    
    def add_log(self, text):
        """Add log message"""
        current = self.log_label.text
        self.log_label.text = current + '\n' + text if current else text
    
    def start_print(self, instance):
        """Start printing"""
        if not BLE_AVAILABLE:
            self.status_label.text = 'HATA: Bleak yüklü değil!'
            return
        
        text = self.text_input.text.strip()
        if not text:
            self.status_label.text = 'HATA: Metin girin!'
            return
        
        self.print_btn.disabled = True
        self.status_label.text = 'Yazdırılıyor...'
        self.progress_bar.value = 0
        
        # Generate image
        try:
            self.current_image = text_to_image(text)
            self.add_log(f'Görsel oluşturuldu: {self.current_image.width}x{self.current_image.height}')
        except Exception as e:
            self.status_label.text = f'HATA: {e}'
            self.print_btn.disabled = False
            return
        
        # Run async print
        asyncio.ensure_future(self.print_async())
    
    async def print_async(self):
        """Async print - printer_app.py ile AYNI"""
        try:
            self.update_status('Yazıcı aranıyor...')
            self.add_log('Tarama başlatıldı...')
            
            device = await BleakScanner.find_device_by_address(DEVICE_ADDRESS, timeout=5.0)
            
            if not device:
                self.update_status('HATA: Yazıcı bulunamadı!')
                self.add_log('HATA: Yazıcı bulunamadı!')
                self.print_btn.disabled = False
                return
            
            self.update_status('Bağlanıyor...')
            self.add_log(f'Yazıcı bulundu: {device.name}')
            
            async with BleakClient(device) as client:
                self.update_status('Bağlandı! Yazdırılıyor...')
                self.add_log('Bağlantı başarılı!')
                
                data = encode_lsb(self.current_image)
                self.add_log(f'Veri hazırlandı: {len(data)} byte')
                
                # Commands - printer_app.py ile AYNI
                self.add_log('Komutlar gönderiliyor...')
                for cmd, delay in [
                    ("2221A70000000000", 0.5),
                    ("2221B10001000000FF", 0.5),
                    ("2221A10001000000FF", 0.5),
                    ("2221A2000100FFFFFF", 1.0),
                    ("2221A9000400000230000000", 1.0),
                ]:
                    await client.write_gatt_char(CMD_UUID, bytes.fromhex(cmd), response=False)
                    await asyncio.sleep(delay)
                
                # Send data - printer_app.py ile AYNI
                total = self.current_image.height
                self.add_log(f'{total} satır gönderiliyor...')
                
                for i in range(total):
                    row = data[i * BYTES_PER_ROW:(i + 1) * BYTES_PER_ROW]
                    await client.write_gatt_char(DATA_UUID, row, response=False)
                    await asyncio.sleep(0.05)
                    
                    # Update progress
                    progress = int((i + 1) / total * 100)
                    Clock.schedule_once(lambda dt: self.update_progress(progress), 0)
                    
                    if (i + 1) % 50 == 0:
                        self.add_log(f'İlerleme: {i + 1}/{total}')
                
                # End - printer_app.py ile AYNI
                self.add_log('Bitiriliyor...')
                await client.write_gatt_char(CMD_UUID, bytes.fromhex("2221AD000100000000"), response=False)
                await asyncio.sleep(2.0)
                
                self.update_status('✅ Yazdırma tamamlandı!')
                self.add_log('✅ Başarılı!')
                
        except Exception as e:
            self.update_status(f'HATA: {e}')
            self.add_log(f'HATA: {e}')
        
        finally:
            Clock.schedule_once(lambda dt: setattr(self.print_btn, 'disabled', False), 0)
    
    def update_status(self, text):
        """Update status label"""
        Clock.schedule_once(lambda dt: setattr(self.status_label, 'text', text), 0)
    
    def update_progress(self, value):
        """Update progress bar"""
        self.progress_bar.value = value

if __name__ == '__main__':
    PrinterApp().run()
