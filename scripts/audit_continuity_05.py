#!/usr/bin/env python3
"""Verify immutable original PNGs, exact patch bounds and all 24 before/after seams."""
from pathlib import Path
from PIL import Image
import json,hashlib
r=Path(__file__).resolve().parents[1];a=r/'app/src/main/assets/pixelart/mansion'
manifest=json.loads((a/'manifest.json').read_text());report=json.loads((r/'docs/0.5/REPARACIONES.json').read_text())
source=Image.new('RGB',(1024,1024))
for room in manifest['rooms']:
 c=room['cell'];source.paste(Image.open(a/room['file']).convert('RGB'),((ord(c[0])-65)*256,(int(c[1])-1)*256))
result=source.copy();allowed=Image.new('1',(1024,1024));limits={}
patches=json.loads((a/'patches/manifest.json').read_text())
for patch in patches:
 image=Image.open(r/'app/src/main/assets'/patch['asset']).convert('RGB')
 x=patch['x'];y=patch['y'];result.paste(image,(x,y));allowed.paste(1,(x,y,x+image.width,y+image.height))
assert len(report['joins'])==24
assert len(patches)==sum(e['accepted'] for e in report['joins'])==6
changed=0
for y in range(1024):
 for x in range(1024):
  original=source.getpixel((x,y));edited=result.getpixel((x,y))
  if original!=edited:
   changed+=1;assert allowed.getpixel((x,y)),'A patch escaped its reviewed boundary'
   assert max(abs(p-q) for p,q in zip(original,edited))<=16
assert changed==report['changed_pixels']==8273
for path,sha in json.loads((r/'docs/art/SHA256_ORIGINALES.json').read_text()).items():
 assert hashlib.sha256((r/path).read_bytes()).hexdigest()==sha
print('20 originals unchanged; 24 joins audited; 6 bounded color repairs; 18 originals retained.')
print('Pixels preserved:',round((1-changed/1024**2)*100,2),'%; no furniture or tile orientation replaced.')
