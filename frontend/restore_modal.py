import os

file_path = r'c:\Users\berna\Desktop\Dev\FutIME\futimeapi\frontend\style.css'

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

target_index = -1
for i, line in enumerate(lines):
    if 'animation: fadeIn 0.3s ease;' in line:
        target_index = i
        break

if target_index != -1:
    # Check for the extra brace
    # Expecting:
    # line i: animation...
    # line i+1: }
    # line i+2: (empty or })
    # line i+3: } (if i+2 was empty)
    
    # Let's look for the next '}' after the one closing .modal
    
    found_first_brace = False
    replace_index = -1
    
    for j in range(target_index + 1, len(lines)):
        stripped = lines[j].strip()
        if stripped == '}':
            if not found_first_brace:
                found_first_brace = True
            else:
                replace_index = j
                break
        elif stripped != '':
            # Found something else before second brace?
            # Maybe I shouldn't be so strict.
            pass

    if replace_index != -1:
        new_content = """
.modal-content {
    background: rgba(15, 30, 45, 0.98);
    border: 1px solid rgba(0, 255, 170, 0.3);
    border-radius: 16px;
    padding: 2rem;
    max-width: 450px;
    width: 90%;
    position: relative;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.6);
    max-height: 90vh;
    overflow-y: auto;
}

.close-modal {
    position: absolute;
    top: 1rem;
    right: 1rem;
    font-size: 1.8rem;
    color: #888;
    cursor: pointer;
    transition: color 0.3s;
}

.close-modal:hover {
    color: #ff6b6b;
}

/* Auth Tabs */
.auth-tabs {
    display: flex;
    gap: 1rem;
    margin-bottom: 2rem;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.auth-tab {
    background: transparent;
    border: none;
    color: #888;
    padding: 0.8rem 1.5rem;
    cursor: pointer;
    font-size: 1rem;
    font-weight: 500;
    transition: all 0.3s;
    border-bottom: 2px solid transparent;
}

.auth-tab.active {
    color: #00ffaa;
    border-bottom-color: #00ffaa;
}

.auth-tab:hover {
    color: #00ffaa;
}

/* Auth Forms */
.auth-form h2 {
    color: #ffffff;
    margin-bottom: 1.5rem;
    font-size: 1.5rem;
}

.form-group {
    margin-bottom: 1.2rem;
}

.form-group label {
    display: block;
    color: #c6d0e3;
    margin-bottom: 0.5rem;
    font-size: 0.9rem;
}

.form-group input {
    width: 100%;
    padding: 0.8rem;
    border-radius: 8px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(0, 0, 0, 0.3);
    color: #fff;
    outline: none;
    transition: all 0.2s;
}

.form-group input:focus {
    border-color: #00ffaa;
    background: rgba(0, 0, 0, 0.5);
}
"""
        lines[replace_index] = new_content
        
        with open(file_path, 'w', encoding='utf-8') as f:
            f.writelines(lines)
        print("Restored modal styles")
    else:
        print("Could not find second brace")
else:
    print("Could not find animation line")
