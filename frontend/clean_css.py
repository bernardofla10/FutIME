import os

file_path = r'c:\Users\berna\Desktop\Dev\FutIME\futimeapi\frontend\style.css'

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
skip_count = 0

for i, line in enumerate(lines):
    if skip_count > 0:
        skip_count -= 1
        continue
    
    stripped = line.strip()
    if stripped == '/* Utility */':
        # Check if next lines match the pattern
        if i + 3 < len(lines):
            l2 = lines[i+1].strip()
            l3 = lines[i+2].strip()
            l4 = lines[i+3].strip()
            
            if l2 == '.hidden {' and l3 == 'display: none !important;' and l4 == '}':
                skip_count = 3
                continue
    
    new_lines.append(line)

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print("Cleaned style.css")
