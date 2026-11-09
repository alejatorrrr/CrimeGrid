#!/usr/bin/env python3
"""Read-only asset verification and seam metrics. Never rewrites approved PNGs."""
from pathlib import Path
from PIL import Image
import hashlib,json,csv
r=Path(__file__).resolve().parents[1]
manifest=json.loads((r/'app/src/main/assets/pixelart/mansion/manifest.json').read_text())
originals=json.loads((r/'docs/art/SHA256_ORIGINALES.json').read_text())
for path,expected in originals.items():
 assert hashlib.sha256((r/path).read_bytes()).hexdigest()==expected,path
images={item['cell']:Image.open(r/'app/src/main/assets/pixelart/mansion'/item['file']).convert('RGB') for item in manifest['rooms']}
assert len(images)==16 and all(im.size==(256,256) for im in images.values())
# Edge differences are a seam diagnostic, not a doorway classifier or a collision mask.
seams=[]
for row in range(1,5):
 for col in range(4):
  a=chr(65+col)+str(row)
  for dr,dc,side in [(0,1,'horizontal'),(1,0,'vertical')]:
   if col+dc>=4 or row+dr>4:continue
   b=chr(65+col+dc)+str(row+dr)
   ia,ib=images[a],images[b]
   pa=[ia.getpixel((255,i) if dc else (i,255)) for i in range(256)]
   pb=[ib.getpixel((0,i) if dc else (i,0)) for i in range(256)]
   mae=sum(abs(x-y) for p,q in zip(pa,pb) for x,y in zip(p,q))/(256*3)
   seams.append(dict(from_cell=a,to_cell=b,axis=side,mean_absolute_RGB_difference=round(mae,2)))
out=r/'docs/art';out.mkdir(parents=True,exist_ok=True)
with (out/'COSTURAS.csv').open('w',newline='') as f:
 w=csv.DictWriter(f,fieldnames=seams[0].keys(),lineterminator="\n");w.writeheader();w.writerows(seams)
sprites={}
for name in ['alma','dario','irene','tomas']:
 sheet=Image.open(r/f'app/src/main/assets/pixelart/characters/{name}_walk_48x64.png')
 assert sheet.mode=='RGBA' and sheet.size==(192,256)
 frames=[];seen={};duplicates=[]
 for row in range(4):
  for frame in range(4):
   im=sheet.crop((frame*48,row*64,(frame+1)*48,(row+1)*64));alpha=im.getchannel('A');ext=alpha.getextrema()
   assert ext==(0,255)
   key=hashlib.sha256(im.tobytes()).hexdigest();label=f'{row}:{frame}'
   if key in seen:duplicates.append([seen[key],label])
   else:seen[key]=label
   frames.append(dict(row=row,frame=frame,bounds=alpha.getbbox(),transparent_pixels=alpha.histogram()[0],opaque_pixels=alpha.histogram()[255]))
 sprites[name]=dict(frames=frames,identical_frames=duplicates)
(out/'SPRITES_AUDITORIA.json').write_text(json.dumps(sprites,indent=2)+'\n')
print('Originals unchanged:',len(originals),'rooms:',len(images),'sprite frames:',sum(len(s['frames']) for s in sprites.values()),'seams:',len(seams))
for name,data in sprites.items():print(name,'identical frames:',data['identical_frames'])
