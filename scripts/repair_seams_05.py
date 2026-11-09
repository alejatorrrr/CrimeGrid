#!/usr/bin/env python3
"""Compose reviewed, narrow architectural patches from an image-edit candidate over immutable originals.
Requires Pillow. No approved tile or sprite is overwritten. Unsafe candidates retain original pixels.
"""
from pathlib import Path
from PIL import Image
import json,hashlib,sys
root=Path(__file__).resolve().parents[1]
assets=root/'app/src/main/assets/pixelart/mansion'
manifest=json.loads((assets/'manifest.json').read_text())
original=Image.new('RGB',(1024,1024))
for room in manifest['rooms']:
 cell=room['cell'];original.paste(Image.open(assets/room['file']).convert('RGB'),((ord(cell[0])-65)*256,(int(cell[1])-1)*256))
if len(sys.argv)<2:raise SystemExit('Pass the reviewed image-edit candidate PNG')
candidate=Image.open(sys.argv[1]).convert('RGB').resize((1024,1024),Image.Resampling.NEAREST)
result=original.copy();entries=[]
def edge_diff(image,a,b):
 x,y=a;xx,yy=b
 return sum(abs(v-w) for v,w in zip(image.getpixel((x,y)),image.getpixel((xx,yy))))/3
for row in range(4):
 for col in range(4):
  for dr,dc in [(0,1),(1,0)]:
   if row+dr>=4 or col+dc>=4:continue
   a=f'{chr(65+col)}{row+1}';b=f'{chr(65+col+dc)}{row+dr+1}'
   if dc:
    line=(col+1)*256;start=row*256+12;stop=(row+1)*256-12
    rect=(line-4,start,line+4,stop)
    before=sum(edge_diff(original,(line-1,y),(line,y)) for y in range(start,stop))/(stop-start)
    after=sum(edge_diff(candidate,(line-1,y),(line,y)) for y in range(start,stop))/(stop-start)
   else:
    line=(row+1)*256;start=col*256+12;stop=(col+1)*256-12
    rect=(start,line-4,stop,line+4)
    before=sum(edge_diff(original,(x,line-1),(x,line)) for x in range(start,stop))/(stop-start)
    after=sum(edge_diff(candidate,(x,line-1),(x,line)) for x in range(start,stop))/(stop-start)
   # Keep every original shape. Only borrow conservative color correction from the image edit.
   # Zero weight at the outer patch boundary avoids adding a second visible seam.
   architectural=row<3 if dc else row<2
   old=original.crop(rect);proposal=candidate.crop(rect);new=old.copy()
   for yy in range(old.height):
    for xx in range(old.width):
     distance=min(xx,old.width-1-xx) if dc else min(yy,old.height-1-yy)
     weight=distance/8
     source=old.getpixel((xx,yy));desired=proposal.getpixel((xx,yy))
     corrected=tuple(v+max(-16,min(16,round((q-v)*weight))) for v,q in zip(source,desired))
     new.putpixel((xx,yy),corrected)
   trial=original.copy();trial.paste(new,rect)
   if dc:after_patch=sum(edge_diff(trial,(line-1,y),(line,y)) for y in range(start,stop))/(stop-start)
   else:after_patch=sum(edge_diff(trial,(x,line-1),(x,line)) for x in range(start,stop))/(stop-start)
   diff=sum(abs(p-q) for px,qx in zip(old.getdata(),new.getdata()) for p,q in zip(px,qx))/(old.width*old.height*3)
   accepted=architectural and after_patch<before*.9
   if accepted:result.paste(new,rect)
   entries.append(dict(join=f'{a}-{b}',rect=rect,before=round(before,2),candidate=round(after,2),after=round(after_patch if accepted else before,2),patch_difference=round(diff,2),accepted=accepted,
     status='reparación cromática acotada; geometría amplia conservada' if accepted else 'original conservado: revisión arquitectónica o paisajística pendiente'))
patch_dir=assets/'patches';patch_dir.mkdir(exist_ok=True)
patch_manifest=[]
for item in entries:
 if item['accepted']:
  rect=tuple(item['rect']);name=item['join']+'.png'
  result.crop(rect).save(patch_dir/name)
  patch_manifest.append(dict(asset='pixelart/mansion/patches/'+name,x=rect[0],y=rect[1]))
(patch_dir/'manifest.json').write_text(json.dumps(patch_manifest,indent=2)+'\n')
result.save(root/'art-review/0.5-atlas.png')
changed=sum(p!=q for p,q in zip(original.getdata(),result.getdata()))
report=dict(candidate_sha256=hashlib.sha256(Path(sys.argv[1]).read_bytes()).hexdigest(),output_sha256=hashlib.sha256((root/'art-review/0.5-atlas.png').read_bytes()).hexdigest(),size=[1024,1024],changed_pixels=changed,total_pixels=1024*1024,joins=entries)
(root/'docs/0.5/REPARACIONES.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print('Accepted:',sum(e['accepted'] for e in entries),'of 24; changed pixels:',changed,'retained:',round(100*(1-changed/1024**2),2),'%')
for e in entries:print(e['join'],e['accepted'],e['before'],e['candidate'],e['patch_difference'])
