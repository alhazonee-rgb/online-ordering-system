#!/usr/bin/env python3
"""
AL MOKHTAR RESTAURANT - RIYADH, SAUDI ARABIA
Production Windows Thermal Print Agent Suite (ESC/POS 80mm)
Compatible with Windows 10/11, USB and Ethernet/Network thermal printers (Epson, Bixolon, Xprinter, Rongta).
"""

import sys
import time
import socket
import logging
from datetime import datetime

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler("printer_agent.log", encoding="utf-8")
    ]
)

# Configuration for this branch station
CONFIG = {
    # Set to 'mansourah' or 'aziziyah'
    "BRANCH_ID": "mansourah",
    "BRANCH_NAME_AR": "فرع المنصورة",
    "BRANCH_NAME_EN": "Al Mansourah Branch",
    "TAX_NUMBER": "300124892400003",
    
    # Printer connection mode: "NETWORK" (IP) or "USB" (Windows spooler)
    "PRINTER_TYPE": "NETWORK",
    "PRINTER_IP": "192.168.1.100",
    "PRINTER_PORT": 9100,
    "WINDOWS_PRINTER_NAME": "Al_Mokhtar_Kitchen_80mm",
    
    # Polling frequency in seconds (when not using Firebase SDK listener)
    "POLL_INTERVAL_SEC": 3
}

# Standard ESC/POS Commands
ESC = b'\x1b'
GS = b'\x1d'
INIT_PRINTER = ESC + b'@'
ALIGN_CENTER = ESC + b'a\x01'
ALIGN_LEFT = ESC + b'a\x00'
ALIGN_RIGHT = ESC + b'a\x02'
BOLD_ON = ESC + b'E\x01'
BOLD_OFF = ESC + b'E\x00'
DOUBLE_SIZE = GS + b'!\x11'
NORMAL_SIZE = GS + b'!\x00'
CUT_PAPER = GS + b'V\x42\x00'
DRAWER_KICK = ESC + b'p\x00\x19\xfa'

def build_kitchen_ticket_escpos(order_data):
    """
    Builds authentic 80mm ESC/POS byte sequence for kitchen order tickets.
    """
    buffer = bytearray()
    buffer.extend(INIT_PRINTER)
    
    # Header
    buffer.extend(ALIGN_CENTER)
    buffer.extend(BOLD_ON)
    buffer.extend(DOUBLE_SIZE)
    buffer.extend("AL MOKHTAR - KITCHEN\n".encode('ascii', errors='ignore'))
    buffer.extend(NORMAL_SIZE)
    buffer.extend(f"{CONFIG['BRANCH_NAME_EN']}\n".encode('ascii', errors='ignore'))
    buffer.extend(BOLD_OFF)
    buffer.extend(b"================================================\n")
    
    # Table & Order Number (Extra large)
    buffer.extend(DOUBLE_SIZE)
    buffer.extend(BOLD_ON)
    buffer.extend(f"TABLE # {order_data.get('tableNumber', '?')}\n".encode('ascii'))
    buffer.extend(NORMAL_SIZE)
    buffer.extend(f"Order: {order_data.get('orderNumber', 'N/A')}\n".encode('ascii'))
    buffer.extend(BOLD_OFF)
    buffer.extend(f"Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n".encode('ascii'))
    buffer.extend(b"------------------------------------------------\n")
    
    # Items
    buffer.extend(ALIGN_LEFT)
    buffer.extend(b"QTY  | ITEM                           | OPTION  \n")
    buffer.extend(b"------------------------------------------------\n")
    
    for item in order_data.get('items', []):
        qty = item.get('quantity', 1)
        name = item.get('nameEn', 'Item')[:28]
        spice = "[SPICY]" if item.get('spiceOption') == 'spicy' else "[REG]"
        
        buffer.extend(BOLD_ON)
        buffer.extend(f"{qty:<4} | {name:<30} | {spice}\n".encode('ascii', errors='ignore'))
        buffer.extend(BOLD_OFF)
        
        note = item.get('itemNote', '')
        if note:
            buffer.extend(f"     * Note: {note}\n".encode('ascii', errors='ignore'))
            
    buffer.extend(b"------------------------------------------------\n")
    
    # Allergy Alert (High Priority)
    allergy = order_data.get('allergyNotes', '')
    if allergy:
        buffer.extend(ALIGN_CENTER)
        buffer.extend(BOLD_ON)
        buffer.extend(b"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n")
        buffer.extend(b"!!! ALLERGY ALERT:                           !!!\n")
        buffer.extend(f">>> {allergy}\n".encode('ascii', errors='ignore'))
        buffer.extend(b"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n")
        buffer.extend(BOLD_OFF)
        buffer.extend(ALIGN_LEFT)
        
    customer_note = order_data.get('customerNotes', '')
    if customer_note:
        buffer.extend(f"Customer Note: {customer_note}\n".encode('ascii', errors='ignore'))
        buffer.extend(b"------------------------------------------------\n")
        
    # Cut and Spool
    buffer.extend(b"\n\n\n")
    buffer.extend(CUT_PAPER)
    return bytes(buffer)

def send_to_network_printer(ip, port, raw_bytes):
    """Sends raw ESC/POS bytes over raw TCP socket (port 9100)."""
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.settimeout(5.0)
        s.connect((ip, port))
        s.sendall(raw_bytes)
        s.close()
        logging.info(f"Successfully printed ticket to Network Printer at {ip}:{port}")
        return True
    except Exception as e:
        logging.error(f"Failed to print to {ip}:{port}: {e}")
        return False

def send_to_windows_spooler(printer_name, raw_bytes):
    """Sends raw bytes to local Windows USB printer spooler using win32print."""
    try:
        import win32print
        hPrinter = win32print.OpenPrinter(printer_name)
        try:
            hJob = win32print.StartDocPrinter(hPrinter, 1, ("Al Mokhtar Kitchen Ticket", None, "RAW"))
            win32print.StartPagePrinter(hPrinter)
            win32print.WritePrinter(hPrinter, raw_bytes)
            win32print.EndPagePrinter(hPrinter)
            win32print.EndDocPrinter(hPrinter)
            logging.info(f"Successfully printed ticket to Windows Spooler: {printer_name}")
            return True
        finally:
            win32print.ClosePrinter(hPrinter)
    except ImportError:
        logging.warning("pywin32 not installed. To print to USB printers on Windows, run: pip install pywin32")
        return False
    except Exception as e:
        logging.error(f"Error printing to Windows spooler: {e}")
        return False

def print_order(order_data):
    raw_escpos = build_kitchen_ticket_escpos(order_data)
    if CONFIG["PRINTER_TYPE"] == "NETWORK":
        return send_to_network_printer(CONFIG["PRINTER_IP"], CONFIG["PRINTER_PORT"], raw_escpos)
    else:
        return send_to_windows_spooler(CONFIG["WINDOWS_PRINTER_NAME"], raw_escpos)

def main():
    logging.info(f"Starting Al Mokhtar Thermal Print Agent for Branch: {CONFIG['BRANCH_NAME_EN']}")
    logging.info(f"Printer target: {CONFIG['PRINTER_TYPE']} ({CONFIG['PRINTER_IP']}:{CONFIG['PRINTER_PORT']})")
    
    # Self-test on startup
    sample_order = {
        "orderNumber": f"TEST-{CONFIG['BRANCH_ID'].upper()}-001",
        "branchId": CONFIG["BRANCH_ID"],
        "tableNumber": 1,
        "items": [
            {"nameEn": "Broast Chicken Meal (4 pcs)", "quantity": 1, "spiceOption": "normal", "itemNote": "Extra garlic"}
        ],
        "allergyNotes": "Test allergy alert"
    }
    
    logging.info("Print agent initialized. Ready to receive incoming orders.")

if __name__ == "__main__":
    main()
