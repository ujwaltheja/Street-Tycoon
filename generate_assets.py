#!/usr/bin/env python3
"""
Generate multi-density Android drawable assets for Street Tycoon game.
All images are created as PNG files under 100KB.
"""

from PIL import Image, ImageDraw, ImageFont
import os

# Base directory for resources
RES_DIR = "./app/src/main/res"

# Define densities and their folders
DENSITIES = {
    'mdpi': 1.0,
    'hdpi': 1.5,
    'xhdpi': 2.0,
    'xxhdpi': 3.0
}

# Character avatars configuration
CHARACTERS = {
    'character_chef': {'color': '#FF6B6B', 'emoji': '👨‍🍳'},
    'character_manager': {'color': '#4ECDC4', 'emoji': '👔'},
    'character_staff': {'color': '#95E1D3', 'emoji': '👷'},
    'character_specialist': {'color': '#F38181', 'emoji': '🎓'}
}

# Stall illustrations configuration
STALLS = {
    'stall_tea': {'color': '#A8E6CF', 'emoji': '🍵'},
    'stall_dosa': {'color': '#FFD3B6', 'emoji': '🥞'},
    'stall_momos': {'color': '#FFAAA5', 'emoji': '🥟'},
    'stall_juice': {'color': '#FF8B94', 'emoji': '🥤'}
}

# Zone backgrounds configuration
ZONES = {
    'bg_zone_marketplace': {'color': '#E8F5E9', 'name': 'Marketplace'},
    'bg_zone_downtown': {'color': '#E3F2FD', 'name': 'Downtown'},
    'bg_zone_residential': {'color': '#FFF3E0', 'name': 'Residential'},
    'bg_zone_business': {'color': '#F3E5F5', 'name': 'Business'},
    'bg_zone_industrial': {'color': '#ECEFF1', 'name': 'Industrial'},
    'bg_zone_commercial': {'color': '#FFF9C4', 'name': 'Commercial'}
}


def create_character_avatar(name, config, size, output_path):
    """Create a character avatar with circular design."""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Draw circle background
    color = config['color']
    margin = size // 10
    draw.ellipse([margin, margin, size-margin, size-margin], fill=color)

    # Add emoji text
    try:
        font_size = size // 2
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", font_size)
    except:
        font = ImageFont.load_default()

    text = config['emoji']
    # Get text bounding box
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]

    # Center the text
    x = (size - text_width) // 2 - bbox[0]
    y = (size - text_height) // 2 - bbox[1]

    draw.text((x, y), text, fill='white', font=font)

    # Save with optimization
    img.save(output_path, 'PNG', optimize=True, quality=85)


def create_stall_illustration(name, config, size, output_path):
    """Create a stall illustration with square design."""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Draw rounded rectangle background
    color = config['color']
    margin = size // 20
    radius = size // 10

    # Draw background with border
    draw.rounded_rectangle([margin, margin, size-margin, size-margin],
                          radius=radius, fill=color)

    # Draw border
    border_color = '#555555'
    draw.rounded_rectangle([margin, margin, size-margin, size-margin],
                          radius=radius, outline=border_color, width=max(2, size//100))

    # Add emoji
    try:
        font_size = size // 2
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", font_size)
    except:
        font = ImageFont.load_default()

    text = config['emoji']
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]

    x = (size - text_width) // 2 - bbox[0]
    y = (size - text_height) // 2 - bbox[1]

    draw.text((x, y), text, fill='white', font=font)

    # Save with optimization
    img.save(output_path, 'PNG', optimize=True, quality=85)


def create_zone_background(name, config, width, height, output_path):
    """Create a zone background with gradient and text."""
    img = Image.new('RGB', (width, height), config['color'])
    draw = ImageDraw.Draw(img)

    # Create subtle gradient
    base_color = config['color']
    for i in range(height):
        alpha = int(255 * (1 - i / height * 0.3))
        draw.rectangle([0, i, width, i+1], fill=base_color)

    # Add zone name text
    try:
        font_size = min(width, height) // 10
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", font_size)
    except:
        font = ImageFont.load_default()

    text = config['name']
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]

    # Position text at top center
    x = (width - text_width) // 2 - bbox[0]
    y = height // 10 - bbox[1]

    # Draw text with shadow
    shadow_offset = max(2, min(width, height) // 200)
    draw.text((x+shadow_offset, y+shadow_offset), text, fill='#00000044', font=font)
    draw.text((x, y), text, fill='#333333', font=font)

    # Add decorative elements (simple grid pattern)
    grid_spacing = min(width, height) // 20
    for i in range(0, width, grid_spacing):
        draw.line([(i, 0), (i, height)], fill='#00000008', width=1)
    for i in range(0, height, grid_spacing):
        draw.line([(0, i), (width, i)], fill='#00000008', width=1)

    # Save with optimization
    img.save(output_path, 'PNG', optimize=True, quality=80)


def main():
    """Generate all multi-density assets."""

    print("🎨 Generating multi-density assets for Street Tycoon...")
    print()

    # Generate character avatars
    print("👥 Generating character avatars...")
    character_sizes = {'mdpi': 96, 'hdpi': 144, 'xhdpi': 192, 'xxhdpi': 288}

    for char_name, char_config in CHARACTERS.items():
        for density, size in character_sizes.items():
            folder = f"{RES_DIR}/drawable-{density}"
            output_path = f"{folder}/{char_name}.png"
            create_character_avatar(char_name, char_config, size, output_path)
            file_size = os.path.getsize(output_path) / 1024
            print(f"  ✓ {char_name} ({density}): {size}x{size}px, {file_size:.1f}KB")

    print()

    # Generate stall illustrations
    print("🏪 Generating stall illustrations...")
    stall_sizes = {'mdpi': 256, 'hdpi': 384, 'xhdpi': 512, 'xxhdpi': 768}

    for stall_name, stall_config in STALLS.items():
        for density, size in stall_sizes.items():
            folder = f"{RES_DIR}/drawable-{density}"
            output_path = f"{folder}/{stall_name}.png"
            create_stall_illustration(stall_name, stall_config, size, output_path)
            file_size = os.path.getsize(output_path) / 1024
            print(f"  ✓ {stall_name} ({density}): {size}x{size}px, {file_size:.1f}KB")

    print()

    # Generate zone backgrounds
    print("🏙️  Generating zone backgrounds...")
    zone_sizes = {
        'mdpi': (480, 640),
        'hdpi': (720, 960),
        'xhdpi': (960, 1280),
        'xxhdpi': (1440, 1920)
    }

    for zone_name, zone_config in ZONES.items():
        for density, (width, height) in zone_sizes.items():
            folder = f"{RES_DIR}/drawable-{density}"
            output_path = f"{folder}/{zone_name}.png"
            create_zone_background(zone_name, zone_config, width, height, output_path)
            file_size = os.path.getsize(output_path) / 1024
            print(f"  ✓ {zone_name} ({density}): {width}x{height}px, {file_size:.1f}KB")

    print()
    print("✨ All assets generated successfully!")
    print()

    # Summary
    total_files = len(CHARACTERS) * 4 + len(STALLS) * 4 + len(ZONES) * 4
    print(f"📊 Summary:")
    print(f"  • Character avatars: {len(CHARACTERS)} × 4 densities = {len(CHARACTERS) * 4} files")
    print(f"  • Stall illustrations: {len(STALLS)} × 4 densities = {len(STALLS) * 4} files")
    print(f"  • Zone backgrounds: {len(ZONES)} × 4 densities = {len(ZONES) * 4} files")
    print(f"  • Total: {total_files} files generated")


if __name__ == "__main__":
    main()
