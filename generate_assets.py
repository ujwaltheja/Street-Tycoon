#!/usr/bin/env python3
"""
Generate multi-density Android drawable assets for Street Tycoon game.
All images are created as PNG files under 100KB.

Requires: Pillow (pip install Pillow)
"""

from PIL import Image, ImageDraw, ImageFont
import os

# Base directory for resources
RES_DIR = "./app/src/main/res"
FONT_DIR = f"{RES_DIR}/font"

# --- Configuration ---

DENSITIES = {
    'mdpi': 1.0,
    'hdpi': 1.5,
    'xhdpi': 2.0,
    'xxhdpi': 3.0
}

CHARACTERS = {
    'character_chef': {'color': '#FF6B6B', 'emoji': '👨‍🍳'},
    'character_manager': {'color': '#4ECDC4', 'emoji': '👔'},
    'character_staff': {'color': '#95E1D3', 'emoji': '👷'},
    'character_specialist': {'color': '#F38181', 'emoji': '🎓'}
}

STALLS = {
    'stall_tea': {'color': '#A8E6CF', 'emoji': '🍵'},
    'stall_dosa': {'color': '#FFD3B6', 'emoji': '🥞'},
    'stall_momos': {'color': '#FFAAA5', 'emoji': '🥟'},
    'stall_juice': {'color': '#FF8B94', 'emoji': '🥤'}
}

ZONES = {
    'bg_zone_marketplace': {'color': '#E8F5E9', 'name': 'Marketplace'},
    'bg_zone_downtown': {'color': '#E3F2FD', 'name': 'Downtown'},
    'bg_zone_residential': {'color': '#FFF3E0', 'name': 'Residential'},
    'bg_zone_business': {'color': '#F3E5F5', 'name': 'Business'},
    'bg_zone_industrial': {'color': '#ECEFF1', 'name': 'Industrial'},
    'bg_zone_commercial': {'color': '#FFF9C4', 'name': 'Commercial'}
}

# --- Font Loading ---

def get_font(size, bold=False):
    """Load a font from the project's font directory."""
    font_file = 'Roboto-Bold.ttf' if bold else 'Roboto-Regular.ttf'
    font_path = os.path.join(FONT_DIR, font_file)
    try:
        return ImageFont.truetype(font_path, size)
    except IOError:
        print(f"Warning: Font '{font_path}' not found. Using default font.")
        return ImageFont.load_default()

# --- Image Generation ---

def create_character_avatar(name, config, size, output_path):
    """Create a character avatar with circular design."""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    draw.ellipse([size // 10, size // 10, size * 9 // 10, size * 9 // 10], fill=config['color'])

    font = get_font(size // 2)
    text = config['emoji']
    bbox = draw.textbbox((0, 0), text, font=font)
    x = (size - (bbox[2] - bbox[0])) // 2 - bbox[0]
    y = (size - (bbox[3] - bbox[1])) // 2 - bbox[1]
    draw.text((x, y), text, fill='white', font=font)

    img.save(output_path, 'PNG', optimize=True, quality=85)

def create_stall_illustration(name, config, size, output_path):
    """Create a stall illustration with square design."""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    margin, radius = size // 20, size // 10
    draw.rounded_rectangle([margin, margin, size - margin, size - margin], radius=radius, fill=config['color'])
    draw.rounded_rectangle([margin, margin, size - margin, size - margin], radius=radius, outline='#555555', width=max(2, size // 100))

    font = get_font(size // 2)
    text = config['emoji']
    bbox = draw.textbbox((0, 0), text, font=font)
    x = (size - (bbox[2] - bbox[0])) // 2 - bbox[0]
    y = (size - (bbox[3] - bbox[1])) // 2 - bbox[1]
    draw.text((x, y), text, fill='white', font=font)

    img.save(output_path, 'PNG', optimize=True, quality=85)

def create_zone_background(name, config, width, height, output_path):
    """Create a zone background with gradient and text."""
    img = Image.new('RGB', (width, height), config['color'])
    draw = ImageDraw.Draw(img)

    font = get_font(min(width, height) // 10, bold=True)
    text = config['name']
    bbox = draw.textbbox((0, 0), text, font=font)
    x = (width - (bbox[2] - bbox[0])) // 2 - bbox[0]
    y = height // 10 - bbox[1]
    draw.text((x + 2, y + 2), text, fill='#00000044', font=font)
    draw.text((x, y), text, fill='#333333', font=font)

    img.save(output_path, 'PNG', optimize=True, quality=80)

# --- Main Execution ---

def main():
    """Generate all multi-density assets."""
    if not os.path.exists(RES_DIR):
        print(f"Error: This script must be run from the project root. Directory '{RES_DIR}' not found.")
        return

    print("🎨 Generating multi-density assets for Street Tycoon...")

    asset_configs = [
        ('👥 Character avatars', CHARACTERS, {'mdpi': 96, 'hdpi': 144, 'xhdpi': 192, 'xxhdpi': 288}, create_character_avatar),
        ('🏪 Stall illustrations', STALLS, {'mdpi': 256, 'hdpi': 384, 'xhdpi': 512, 'xxhdpi': 768}, create_stall_illustration),
        ('🏙️ Zone backgrounds', ZONES, {'mdpi': (480, 640), 'hdpi': (720, 960), 'xhdpi': (960, 1280), 'xxhdpi': (1440, 1920)}, create_zone_background)
    ]

    for title, items, sizes, generator_func in asset_configs:
        print(f"\n{title}...")
        for name, config in items.items():
            for density, size_or_dims in sizes.items():
                folder = f"{RES_DIR}/drawable-{density}"
                os.makedirs(folder, exist_ok=True)
                output_path = f"{folder}/{name}.png"
                if isinstance(size_or_dims, tuple):
                    generator_func(name, config, size_or_dims[0], size_or_dims[1], output_path)
                    dims_str = f"{size_or_dims[0]}x{size_or_dims[1]}px"
                else:
                    generator_func(name, config, size_or_dims, output_path)
                    dims_str = f"{size_or_dims}x{size_or_dims}px"
                file_size = os.path.getsize(output_path) / 1024
                print(f"  ✓ {name} ({density}): {dims_str}, {file_size:.1f}KB")

    print("\n✨ All assets generated successfully!\n")

if __name__ == "__main__":
    main()
