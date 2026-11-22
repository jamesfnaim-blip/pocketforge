import os
import sys
import shutil
import subprocess
import threading
import time
from http.server import SimpleHTTPRequestHandler, HTTPServer

PORT = 8765

def find_chrome():
    # Windows paths
    win_paths = [
        r"C:\Program Files\Google\Chrome\Application\chrome.exe",
        r"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe",
        r"C:\Users\Naim Hossain\AppData\Local\Google\Chrome\Application\chrome.exe",
    ]
    for p in win_paths:
        if os.path.exists(p):
            return p

    # Linux / CI paths
    for name in ["google-chrome", "google-chrome-stable", "chromium", "chromium-browser"]:
        found = shutil.which(name)
        if found:
            return found

    return None

def main():
    chrome = find_chrome()
    if not chrome:
        print("ERROR: Chrome or Chromium executable not found!")
        sys.exit(1)

    print(f"Using browser at: {chrome}")

    out_dir = os.path.abspath("docs/ui-review")
    os.makedirs(out_dir, exist_ok=True)

    # Start local HTTP server
    server = HTTPServer(("127.0.0.1", PORT), SimpleHTTPRequestHandler)
    server_thread = threading.Thread(target=server.serve_forever, daemon=True)
    server_thread.start()
    print(f"Started local preview server at http://127.0.0.1:{PORT}")

    screens = [
        ("setup_lang", "setup-lang", "Setup - Language Selection"),
        ("setup_hardware", "setup-hardware", "Setup - Hardware Preflight"),
        ("setup_install", "setup-install", "Setup - Subsystem Installation"),
        ("setup_providers", "setup-providers", "Setup - Provider Key Vault"),
        ("home", "home", "Home Dashboard"),
        ("chat_simple", "chat", "Agent Chat (Simple Mode)"),
        ("bug_report", "bug-report", "Bug Report & Diagnostics"),
        ("developer", "developer", "Developer & Credits"),
    ]

    themes = [
        ("obsidian", "dark", "Obsidian Dark"),
        ("amoled", "amoled", "AMOLED Black"),
        ("light", "light", "Clean Light"),
    ]

    languages = [
        ("en", "English"),
        ("bn", "বাংলা"),
    ]

    captured_files = []

    try:
        for screen_key, screen_id, screen_title in screens:
            for theme_key, theme_val, theme_title in themes:
                for lang_key, lang_title in languages:
                    file_name = f"{screen_key}_{theme_key}_{lang_key}.png"
                    file_path = os.path.join(out_dir, file_name)

                    url = (
                        f"http://127.0.0.1:{PORT}/docs/mockups/index.html"
                        f"?screen={screen_id}&theme={theme_val}&lang={lang_key}&frameOnly=true"
                    )

                    cmd = [
                        chrome,
                        "--headless=new",
                        "--disable-gpu",
                        "--no-sandbox",
                        "--disable-dev-shm-usage",
                        f"--screenshot={file_path}",
                        "--window-size=430,940",
                        url,
                    ]

                    subprocess.run(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

                    if os.path.exists(file_path) and os.path.getsize(file_path) > 1000:
                        size_kb = os.path.getsize(file_path) // 1024
                        print(f"Captured: {file_name} ({size_kb} KB)")
                        captured_files.append((file_name, screen_title, theme_title, lang_title))
                    else:
                        print(f"Warning: Failed to capture {file_name}")

    finally:
        server.shutdown()
        print("Stopped preview server.")

    # Generate Gallery HTML Viewer
    gallery_html_path = os.path.join(out_dir, "index.html")
    with open(gallery_html_path, "w", encoding="utf-8") as f:
        f.write('''<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>PocketForge AI - UI Review Gallery</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0D0F12; color: #F1F5F9; margin: 0; padding: 24px; }
    h1 { margin: 0 0 6px 0; font-size: 24px; color: #6366F1; }
    p { margin: 0 0 20px 0; color: #94A3B8; font-size: 14px; }
    .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
    .card { background: #16191E; border: 1px solid #262B35; border-radius: 12px; padding: 12px; text-align: center; }
    .card img { max-width: 100%; height: auto; border-radius: 8px; border: 1px solid #333; }
    .title { font-weight: 600; font-size: 14px; margin-top: 10px; color: #F1F5F9; }
    .meta { font-size: 12px; color: #06B6D4; margin-top: 4px; }
  </style>
</head>
<body>
  <h1>PocketForge AI UI Review Gallery</h1>
  <p>Automated verification gallery across Obsidian, AMOLED, and Light modes for English and Bengali.</p>
  <div class="grid">
''')
        for file_name, screen_title, theme_title, lang_title in captured_files:
            f.write(f'''    <div class="card">
      <img src="{file_name}" alt="{screen_title} ({theme_title}, {lang_title})" loading="lazy">
      <div class="title">{screen_title}</div>
      <div class="meta">{theme_title} • {lang_title}</div>
    </div>\n''')
        f.write('''  </div>
</body>
</html>
''')

    print(f"\nGenerated gallery with {len(captured_files)} images at {gallery_html_path}")

if __name__ == "__main__":
    main()
