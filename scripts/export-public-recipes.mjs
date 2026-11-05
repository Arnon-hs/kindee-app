import {writeFile} from 'node:fs/promises';
const response=await fetch('http://localhost:8080/api/v1/recipes?market=TH',{headers:{Accept:'application/json'}});
if(!response.ok)throw new Error(`Public recipe export failed: ${response.status}`);
const {data}=await response.json();
if(!Array.isArray(data)||!data.length)throw new Error('Public recipe export is empty');
const recipes=data.map(r=>{
 const allowed=['slug','title','description','servings','total_minutes','cleanup_minutes','nutrition','market','price_currency','provenance','allergen_status','ingredients'];
 if(!r.slug||!r.nutrition||r.provenance!=='illustrative_seed_v0.1')throw new Error('Unexpected recipe contract');
 return Object.fromEntries(allowed.map(key=>[key,r[key]]));
});
await writeFile(new URL('../src/data/public-recipes.json',import.meta.url),JSON.stringify({source:'/api/v1/recipes?market=TH',generated_at:new Date().toISOString(),kind:'illustrative_seed_snapshot',recipes},null,2)+'\n');
console.log(`Exported ${recipes.length} public recipe snapshots; no personal endpoints used.`);
