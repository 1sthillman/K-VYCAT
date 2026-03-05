#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
MXW01 Printer - Kivy Android App
Android native Bluetooth kullanarak yazdırma
"""

import time
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput
from kivy.uix.progressbar import ProgressBar
from kivy.uix.scrollview import ScrollView
from kivy.clock import Clock
from kivy.utils import platform
from PIL import Image, ImageDraw, ImageFont
import io

# Android Bluetooth
BLE_AVAILABLE = False
if platform == 'android':
    try:
        from jnius import autoclass, cast
        from android.permissions import request_permissions, Permission
        
        # Android classes
        BluetoothAdapter = autoclass('android.bluetooth.BluetoothAdapter')
        BluetoothDevice = autoclass('android.bluetooth.BluetoothDevice')
        BluetoothGatt = autoclass('android.bluetooth.BluetoothGatt')
        BluetoothGattCallback = autoclass('android.bluetooth.BluetoothGattCallback')
        BluetoothGattCharacteristic = autoclass('android.bluetooth.BluetoothGattCharacteristic')
        UUID = autoclass('java.util.UUID')
        
        BLE_AVAILABLE = True
    except Exception as e:
        print(f"Android Bluetooth import error: {e}")
        BLE_AVAILABLE = False

# Printer Protocol
DEVICE_ADDRESS = "48:0F:57:3E:60:77"
CMD_UUID = "0000ae01-0000-1000-8000-00805f9b34fb"
DATA_UUID = "0000ae03-0000-1000-8000-00805f9b34fb"
PRINTER_WIDTH = 384
BYTES_PER_ROW = 48

# Android Bluetooth Manager
class AndroidBleManager:
    """Android native Bluetooth Low Energy manager"""
    
    def __init__(self):
        self.gatt = None
        self.cmd_char = None
        self.data_char = None
        self.connected = False
        
    def connect(self, address, callback):
        """Connect to device"""
        if not BLE_AVAILABLE:
            callback(False, "Bluetooth not available")
            return
        
        try:
            adapter = BluetoothAdapter.getDefaultAdapter()
            if not adapter:
                callback(False, "No Bluetooth adapter")
                return
            
            device = adapter.getRemoteDevice(address)
            
            # GATT callback
            class GattCallback(BluetoothGattCallback):
                def __init__(self, manager, cb):
                    super().__init__()
                    self.manager = manager
                    self.cb = cb
                
                def onConnectionStateChange(self, gatt, status, newState):
                    if newState == 2:  # STATE_CONNECTED
                        gatt.discoverServices()
                    else:
                        self.manager.connected = False
                        self.cb(False, "Disconnected")
                
                def onServicesDiscovered(self, gatt, status):
                    if status == 0:  # GATT_SUCCESS
                        # Find characteristics
                        cmd_uuid = UUID.fromString(CMD_UUID)
                        data_uuid = UUID.fromString(DATA_UUID)
                        
                        for service in gatt.getServices().toArray():
                            for char in service.getCharacteristics().toArray():
                                char_uuid = char.getUuid()
                                if char_uuid.equals(cmd_uuid):
                                    self.manager.cmd_char = char
                                elif char_uuid.equals(data_uuid):
                                    self.manager.data_char = char
                        
                        if self.manager.cmd_char and self.manager.data_char:
                            self.manager.connected = True
                            self.manager.gatt = gatt
                            self.cb(True, "Connected")
                        else:
                            self.cb(False, "Characteristics not found")
                    else:
                        self.cb(False, f"Service discovery failed: {status}")
            
            gatt_callback = GattCallback(self, callback)
            self.gatt = device.connectGatt(None, False, gatt_callback)
            
        except Exception as e:
            callback(False, str(e))
    
    def write_cmd(self, data):
        """Write to command characteristic"""
        if not self.connected or not self.cmd_char:
            return False
        
        try:
            self.cmd_char.setValue(data)
            self.cmd_char.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
            return self.gatt.writeCharacteristic(self.cmd_char)
        except Exception as e:
            print(f"Write CMD error: {e}")
            return False
    
    def write_data(self, data):
        """Write to data characteristic"""
        if not self.connected or not self.data_char:
            return False
        
        try:
            self.data_char.setValue(data)
            self.data_char.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
            return self.gatt.writeCharacteristic(self.data_char)
        except Exception as e:
            print(f"Write DATA error: {e}")
            return False
    
    def disconnect(self):
        """Disconnect"""
        if self.gatt:
            self.gatt.disconnect()
            self.gatt.close()
            self.gatt = None
        self.connected = False

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
        
        # Request Android permissions
        if platform == 'android':
            request_permissions([
                Permission.BLUETOOTH,
                Permission.BLUETOOTH_ADMIN,
                Permission.BLUETOOTH_SCAN,
                Permission.BLUETOOTH_CONNECT,
                Permission.ACCESS_FINE_LOCATION,
                Permission.ACCESS_COARSE_LOCATION
            ])
        
        # Main layout
        layout = BoxLayout(orientation='vertical', padding=10, spacing=10)
        
        # Status label
        self.status_label = Label(
            text='Hazır' if BLE_AVAILABLE else 'HATA: Bluetooth kullanılamıyor!',
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
        self.ble_manager = AndroidBleManager() if BLE_AVAILABLE else None
        
        return layout
    
    def add_log(self, text):
        """Add log message"""
        current = self.log_label.text
        self.log_label.text = current + '\n' + text if current else text
    
    def start_print(self, instance):
        """Start printing"""
        if not BLE_AVAILABLE:
            self.status_label.text = 'HATA: Bluetooth kullanılamıyor!'
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
        
        # Connect and print
        self.add_log('Bağlanıyor...')
        self.ble_manager.connect(DEVICE_ADDRESS, self.on_connected)
    
    def on_connected(self, success, message):
        """Connection callback"""
        if not success:
            Clock.schedule_once(lambda dt: self.update_status(f'HATA: {message}'), 0)
            Clock.schedule_once(lambda dt: self.add_log(f'HATA: {message}'), 0)
            Clock.schedule_once(lambda dt: setattr(self.print_btn, 'disabled', False), 0)
            return
        
        Clock.schedule_once(lambda dt: self.add_log('Bağlandı!'), 0)
        Clock.schedule_once(lambda dt: self.print_data(), 0)
    
    def print_data(self):
        """Print data to printer"""
        try:
            data = encode_lsb(self.current_image)
            self.add_log(f'Veri hazırlandı: {len(data)} byte')
            
            # Send commands
            self.add_log('Komutlar gönderiliyor...')
            commands = [
                ("2221A70000000000", 0.5),
                ("2221B10001000000FF", 0.5),
                ("2221A10001000000FF", 0.5),
                ("2221A2000100FFFFFF", 1.0),
                ("2221A9000400000230000000", 1.0),
            ]
            
            for cmd_hex, delay in commands:
                cmd_bytes = bytes.fromhex(cmd_hex)
                if not self.ble_manager.write_cmd(cmd_bytes):
                    self.add_log(f'HATA: Komut gönderilemedi: {cmd_hex}')
                    self.print_btn.disabled = False
                    return
                time.sleep(delay)
            
            # Send data rows
            total = self.current_image.height
            self.add_log(f'{total} satır gönderiliyor...')
            
            for i in range(total):
                row = data[i * BYTES_PER_ROW:(i + 1) * BYTES_PER_ROW]
                
                if not self.ble_manager.write_data(row):
                    self.add_log(f'HATA: Satır {i} gönderilemedi')
                    break
                
                time.sleep(0.05)
                
                # Update progress
                progress = int((i + 1) / total * 100)
                Clock.schedule_once(lambda dt, p=progress: self.update_progress(p), 0)
                
                if (i + 1) % 50 == 0:
                    Clock.schedule_once(lambda dt, idx=i: self.add_log(f'İlerleme: {idx + 1}/{total}'), 0)
            
            # End command
            self.add_log('Bitiriliyor...')
            self.ble_manager.write_cmd(bytes.fromhex("2221AD000100000000"))
            time.sleep(2.0)
            
            # Disconnect
            self.ble_manager.disconnect()
            
            Clock.schedule_once(lambda dt: self.update_status('✅ Yazdırma tamamlandı!'), 0)
            Clock.schedule_once(lambda dt: self.add_log('✅ Başarılı!'), 0)
            
        except Exception as e:
            Clock.schedule_once(lambda dt: self.update_status(f'HATA: {e}'), 0)
            Clock.schedule_once(lambda dt: self.add_log(f'HATA: {e}'), 0)
        
        finally:
            Clock.schedule_once(lambda dt: setattr(self.print_btn, 'disabled', False), 0)
    
    def update_status(self, text):
        """Update status label"""
        self.status_label.text = text
    
    def update_progress(self, value):
        """Update progress bar"""
        self.progress_bar.value = value

if __name__ == '__main__':
    PrinterApp().run()
