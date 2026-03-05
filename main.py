#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
MXW01 Kivy Printer App - Bleak ile
"""

import asyncio
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput
from kivy.uix.image import Image as KivyImage
from kivy.uix.progressbar import ProgressBar
from kivy.uix.scrollview import ScrollView
from kivy.clock import Clock
from kivy.graphics.texture import Texture
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
    """LSB encoding"""
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
    """Text to bitmap"""
    lines = text.split('\n') if text.strip() else ["Test"]
    
    # Font
    try:
        font = ImageFont.truetype("/system/fonts/Roboto-Bold.ttf", font_size)
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
            text='Hazır',
            size_hint=(1, 0.1),
            color=(1, 1, 1, 1)
        )
        layout.add_widget(self.status_label)
        
        # Text input
        self.text_input = TextInput(
            hint_text='Yazdırmak istediğiniz metni girin...',
            multiline=True,
            size_hint=(1, 0.3)
        )
        self.text_input.bind(text=self.update_preview)
        layout.add_widget(self.text_input)
        
        # Preview
        preview_scroll = ScrollView(size_hint=(1, 0.3))
        self.preview_image = KivyImage()
        preview_scroll.add_widget(self.preview_image)
        layout.add_widget(preview_scroll)
        
        # Progress bar
        self.progress_bar = ProgressBar(max=100, size_hint=(1, 0.05))
        layout.add_widget(self.progress_bar)
        
        # Print button
        self.print_btn = Button(
            text='Yazdır',
            size_hint=(1, 0.15),
            background_color=(0.2, 0.6, 1, 1)
        )
        self.print_btn.bind(on_press=self.start_print)
        layout.add_widget(self.print_btn)
        
        # Initial preview
        self.current_image = None
        self.update_preview(None, "Test")
        
        return layout
    
    def update_preview(self, instance, text):
        """Update preview image"""
        try:
            if not text.strip():
                text = "Test"
            
            # Generate image
            self.current_image = text_to_image(text)
            
            # Convert to Kivy texture
            img_rgb = self.current_image.convert('RGB')
            buf = io.BytesIO()
            img_rgb.save(buf, format='PNG')
            buf.seek(0)
            
            texture = Texture.create(size=(img_rgb.width, img_rgb.height))
            texture.blit_buffer(img_rgb.tobytes(), colorfmt='rgb', bufferfmt='ubyte')
            texture.flip_vertical()
            
            self.preview_image.texture = texture
            
        except Exception as e:
            self.status_label.text = f'Hata: {e}'
    
    def start_print(self, instance):
        """Start printing"""
        if not BLE_AVAILABLE:
            self.status_label.text = 'HATA: Bleak yüklü değil!'
            return
        
        if self.current_image is None:
            self.status_label.text = 'HATA: Görsel yok!'
            return
        
        self.print_btn.disabled = True
        self.status_label.text = 'Yazdırılıyor...'
        self.progress_bar.value = 0
        
        # Run async print
        asyncio.ensure_future(self.print_async())
    
    async def print_async(self):
        """Async print function"""
        try:
            self.update_status('Yazıcı aranıyor...')
            
            device = await BleakScanner.find_device_by_address(DEVICE_ADDRESS, timeout=5.0)
            
            if not device:
                self.update_status('HATA: Yazıcı bulunamadı!')
                self.print_btn.disabled = False
                return
            
            self.update_status('Bağlanıyor...')
            
            async with BleakClient(device) as client:
                self.update_status('Bağlandı! Yazdırılıyor...')
                
                data = encode_lsb(self.current_image)
                
                # Commands
                for cmd, delay in [
                    ("2221A70000000000", 0.5),
                    ("2221B10001000000FF", 0.5),
                    ("2221A10001000000FF", 0.5),
                    ("2221A2000100FFFFFF", 1.0),
                    ("2221A9000400000230000000", 1.0),
                ]:
                    await client.write_gatt_char(CMD_UUID, bytes.fromhex(cmd), response=False)
                    await asyncio.sleep(delay)
                
                # Send data
                total = self.current_image.height
                for i in range(total):
                    row = data[i * BYTES_PER_ROW:(i + 1) * BYTES_PER_ROW]
                    await client.write_gatt_char(DATA_UUID, row, response=False)
                    await asyncio.sleep(0.05)
                    
                    # Update progress
                    progress = int((i + 1) / total * 100)
                    Clock.schedule_once(lambda dt: self.update_progress(progress), 0)
                
                # End
                await client.write_gatt_char(CMD_UUID, bytes.fromhex("2221AD000100000000"), response=False)
                await asyncio.sleep(2.0)
                
                self.update_status('✅ Yazdırma tamamlandı!')
                
        except Exception as e:
            self.update_status(f'HATA: {e}')
        
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
