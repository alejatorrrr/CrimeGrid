#!/usr/bin/env python3
"""Audit exact imported bytes, source crop coverage and 24 castle joins; no image is rewritten."""
from pathlib import Path
from PIL import Image
import json,hashlib,csv
r=Path(__file__).resolve().parents[1];a=r/'app/src/main/assets/pixelart/castle003'
m=json.loads((a/'manifest.json').read_text());expected=json.loads((r/'docs/castle003/ORIGINALES_SHA256.json').read_text())
assert m['source_size']==[1536,1536] and m['tile_size']==[384,384]
assert len(m['rooms'])==16
atlas=Image.new('RGB',(1536,1536));images={};ids=set()
for room in m['rooms']:
 path=a/room['file'];data=path.read_bytes();assert hashlib.sha256(data).hexdigest()==expected[room['file']]
 im=Image.open(path).convert('RGB');assert im.size==(384,384)
 cell=room['id'];assert cell not in ids;ids.add(cell)
 x=(ord(cell[0])-65)*384;y=(int(cell[1])-1)*384
 assert room['source_crop']==[x,y,x+384,y+384]
 images[cell]=im;atlas.paste(im,(x,y))
 assert atlas.crop((x,y,x+384,y+384)).tobytes()==im.tobytes()
assert ids=={f'{c}{n}' for n in range(1,5) for c in 'ABCD'}
rows=[]
for row in range(1,5):
 for col in range(4):
  cell=chr(65+col)+str(row)
  for dr,dc in [(0,1),(1,0)]:
   if row+dr>4 or col+dc>=4:continue
   neighbor=chr(65+col+dc)+str(row+dr)
   left=images[cell];right=images[neighbor]
   diff=sum(abs(x-y) for i in range(384) for x,y in zip(left.getpixel((383,i)if dc else(i,383)),right.getpixel((0,i)if dc else(i,0))))/(384*3)
   rows.append(dict(join=f'{cell}-{neighbor}',axis='horizontal'if dc else'vertical',mean_edge_rgb_difference=round(diff,2)))
assert len(rows)==24
with (r/'docs/castle003/UNIONES.csv').open('w',newline='')as f:
 writer=csv.DictWriter(f,fieldnames=rows[0].keys(),lineterminator='\n');writer.writeheader();writer.writerows(rows)
print('16 original PNG hashes verified. 1536x1536 exact assembly; 24 adjacent joins; no missing/duplicated bands.')
print('No original full source image was supplied: byte-for-byte comparison against that parent image is not possible.')

logical=json.loads((r/'docs/castle003/LOGICA_003_BASE.json').read_text())
case=json.loads((r/'core/src/main/resources/cases/case003.json').read_text())
for key,value in logical.items():assert case[key]==value,'Case 003 logical data changed: '+key
print('Case 003 suspects, clues, hints, crime position and unique solution unchanged; only narrative metadata adapted.')
