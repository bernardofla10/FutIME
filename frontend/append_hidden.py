import os

file_path = r'c:\Users\berna\Desktop\Dev\FutIME\futimeapi\frontend\style.css'

with open(file_path, 'a', encoding='utf-8') as f:
    f.write('\n\n/* Utility */\n.hidden {\n    display: none !important;\n}\n')

print("Appended .hidden")
