#!/usr/bin/env python3
"""
Generate Android vector drawable assets for Street Tycoon game.
"""

import os

# Base directory for resources
RES_DIR = "./app/src/main/res/drawable"

# Icon definitions (using placeholder paths)
ICONS = {
    "ui_controls": {
        "ic_btn_tap_serve_default": "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2z",
        "ic_btn_tap_serve_pressed": "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2z",
        "ic_btn_upgrade": "M12,8l-6,6 1.41,1.41L12,10.83l4.59,4.58L18,14l-6,-6z",
        "ic_btn_helpers": "M16,11c1.66,0 2.99,-1.34 2.99,-3S17.66,5 16,5c-1.66,0 -3,1.34 -3,3s1.34,3 3,3zm-8,0c1.66,0 2.99,-1.34 2.99,-3S9.66,5 8,5C6.34,5 5,6.34 5,8s1.34,3 3,3zm0,2c-2.33,0 -7,1.17 -7,3.5V19h14v-2.5c0,-2.33 -4.67,-3.5 -7,-3.5zm8,0c-.29,0 -.62,.02 -.97,.05 1.16,.84 1.97,1.97 1.97,3.45V19h6v-2.5c0,-2.33 -4.67,-3.5 -7,-3.5z",
        "ic_btn_shop": "M7,18c-1.1,0 -1.99,.9 -1.99,2S5.9,22 7,22s2,-.9 2,-2 -.9,-2 -2,-2zM1,2v2h2l3.6,7.59 -1.35,2.44C4.52,15.37 5.24,17 6.5,17H20v-2H6.5c-.3,0 -.54,-.24 -.54,-.54L6,13.71l1.9-3.41h7.45c.75,0 1.41,-.41 1.75,-1.03l3.58,-6.49C21.25,2.62 20.75,2 20.01,2H5.21L4.27,0H1zm16,16c-1.1,0 -1.99,.9 -1.99,2s.9,2 2,2 2,-.9 2,-2 -.9,-2 -2,-2z",
        "ic_btn_settings": "M19.43,12.98c.04,-.32 .07,-.64 .07,-.98s-.03,-.66 -.07,-.98l2.11,-1.65c.19,-.15 .24,-.42 .12,-.64l-2,-3.46c-.12,-.22 -.39,-.3 -.61,-.22l-2.49,1c-.52,-.4 -1.08,-.73 -1.69,-.98l-.38,-2.65C14.46,2.18 14.25,2 14,2h-4c-.25,0 -.46,.18 -.49,.42l-.38,2.65c-.61,.25 -1.17,.59 -1.69,.98l-2.49,-1c-.23,-.09 -.49,0 -.61,.22l-2,3.46c-.13,.22 -.07,.49 .12,.64l2.11,1.65c-.04,.32 -.07,.64 -.07,.98s.03,.66 .07,.98l-2.11,1.65c-.19,.15 -.24,.42 -.12,.64l2,3.46c.12,.22 .39,.3 .61,.22l2.49,-1c.52,.4 1.08,.73 1.69,.98l.38,2.65c.03,.24 .24,.42 .49,.42h4c.25,0 .46,-.18 .49,-.42l.38,-2.65c.61,-.25 1.17,-.59 1.69,-.98l2.49,1c.23,.09 .49,0 .61,-.22l2,-3.46c.12,-.22 .07,-.49 -.12,-.64l-2.11,-1.65zM12,15.5c-1.93,0 -3.5,-1.57 -3.5,-3.5s1.57,-3.5 3.5,-3.5 3.5,1.57 3.5,3.5 -1.57,3.5 -3.5,3.5z",
        "ic_btn_close": "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z",
        "ic_btn_back": "M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z",
        "ic_btn_home": "M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z"
    },
    "currency": {
        "ic_rupee": "M10,18.01h1v.01h-1V18.01z M8.5,13H15v-2H8.5c-1.1,0 -2,-.9 -2,-2s.9,-2 2,-2H14V5h-1.5v2H10V5H8v2H5.5C4.12,7 3,8.12 3,9.5S4.12,12 5.5,12H8v1H5v2h3.5z M8.5,9H12v2H8.5c-.28,0 -.5,.22 -.5,.5s.22,.5 .5,.5H12v2H8.5c-.28,0 -.5,.22 -.5,.5S8.22,15 8.5,15H12v-2H8.5c-1.1,0 -2,-.9 -2,-2s.9,-2 2,-2z",
        "ic_coins": "M16,11c1.66,0 2.99,-1.34 2.99,-3S17.66,5 16,5c-1.66,0 -3,1.34 -3,3s1.34,3 3,3zm-8,0c1.66,0 2.99,-1.34 2.99,-3S9.66,5 8,5C6.34,5 5,6.34 5,8s1.34,3 3,3zm0,2c-2.33,0 -7,1.17 -7,3.5V19h14v-2.5c0,-2.33 -4.67,-3.5 -7,-3.5zm8,0c-.29,0 -.62,.02 -.97,.05 1.16,.84 1.97,1.97 1.97,3.45V19h6v-2.5c0,-2.33 -4.67,-3.5 -7,-3.5z"
    }
}

def create_vector_drawable(path_data):
    return f'''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24.0"
    android:viewportHeight="24.0">
    <path
        android:fillColor="#FF000000"
        android:pathData="{path_data}"/>
</vector>'''

def main():
    """Generate all vector drawable assets."""
    print("🎨 Generating vector drawable assets for Street Tycoon...")

    for category, icons in ICONS.items():
        category_dir = os.path.join(RES_DIR, category)
        os.makedirs(category_dir, exist_ok=True)
        for name, path_data in icons.items():
            file_path = os.path.join(category_dir, f"{name}.xml")
            with open(file_path, "w") as f:
                f.write(create_vector_drawable(path_data))
            print(f"  ✓ {name}.xml")

    print("\n✨ All vector assets generated successfully!\n")

if __name__ == "__main__":
    main()
