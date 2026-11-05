import catalog from './data/public-recipes.json';
import {defaultProfile,validateProfile,type ProfileInput} from './profile';
import type {Dashboard} from './api/dashboard';
import type {Recipe, Nutrition, Nutrients} from './api/recipes';
export const publicCatalog = catalog.recipes as Recipe[];
export const snapshotDate = catalog.generated_at;
const nutrients=['calories','protein_g','fat_g','carbs_g','price_thb'] as const;
const clone=<T>(value:T):T=>JSON.parse(JSON.stringify(value));
export function scalePublicRecipe(slug:string,servings:number):Nutrition {
 if(!Number.isInteger(servings)||servings<1||servings>30)throw new Error('invalidServings');
 const recipe=publicCatalog.find(r=>r.slug===slug);
 if(!recipe)throw new Error('recipeNotFound');
 const perServing=clone(recipe.nutrition.per_serving);
 const total=Object.fromEntries(nutrients.map(key=>[key,perServing[key]===null?null:Math.round(perServing[key]*servings*100)/100])) as unknown as Nutrients;
 return {servings,total,per_serving:perServing,is_price_estimate:true};
}
export class DemoSession {
 private profile:ProfileInput={...defaultProfile};
 private nextId=1;
 private entries:{id:number;slug:string;at:string;nutrition:Nutrition}[]=[];
 private weights:{id:number;kg:string;recorded_at:string}[]=[];
 constructor(private readonly clock:()=>Date=()=>new Date()){}
 clear():void{this.nextId=1;this.entries=[];this.weights=[];this.profile={...defaultProfile};}
 setProfile(profile:ProfileInput):void{this.profile=validateProfile(profile);}
 addFood(slug:string,servings:number):void{if(!publicCatalog.some(r=>r.slug===slug&&(r as Recipe&{market?:string}).market===this.profile.market))throw new Error('recipeNotFound');const nutrition=scalePublicRecipe(slug,servings);this.entries.push({id:this.nextId++,slug,at:this.clock().toISOString(),nutrition:clone(nutrition)});}
 addWeight(kg:number):void{if(!Number.isFinite(kg)||kg<20||kg>400)throw new Error('invalidWeight');this.weights.push({id:this.nextId++,kg:kg.toFixed(2),recorded_at:this.clock().toISOString()});this.weights=this.weights.slice(-30);}
 removeFood(id:number):void{if(!this.entries.some(e=>e.id===id))throw new Error('entryNotFound');this.entries=this.entries.filter(e=>e.id!==id);}
 removeWeight(id:number):void{if(!this.weights.some(e=>e.id===id))throw new Error('entryNotFound');this.weights=this.weights.filter(e=>e.id!==id);}
 dashboard():Dashboard {
  const day=(date:Date)=>date.toLocaleDateString('en-CA',{timeZone:this.profile.timezone});
  const entries=this.entries.filter(entry=>day(new Date(entry.at))===day(this.clock()));
  const consumed={calories:0,protein_g:0,fat_g:0,carbs_g:0};
  for(const entry of entries)for(const key of ['calories','protein_g','fat_g','carbs_g'] as const)consumed[key]=Math.round((consumed[key]+entry.nutrition.total[key])*100)/100;
  const recommendations=publicCatalog.filter(r=>(r as Recipe&{market?:string}).market===this.profile.market).map(r=>({slug:r.slug,title:r.title,minutes:r.total_minutes,cleanup_minutes:r.cleanup_minutes,nutrition:clone(r.nutrition.per_serving)})).sort((a,b)=>a.cleanup_minutes-b.cleanup_minutes||a.minutes-b.minutes);
  return {date:day(this.clock()),timezone:this.profile.timezone,market:{country_code:this.profile.market,currency:this.profile.market==='TH'?'THB':'USD'},nutrition:{consumed,goals:{calories:this.profile.calories,protein_g:this.profile.protein_g,fat_g:this.profile.fat_g,carbs_g:this.profile.carbs_g}},recommendations,meal_prep:{status:'suggestion',recipe:recommendations[0]??null,portions:3,saved_plan:false},weight:{unit:'kg',logs:clone(this.weights)},food_entries:entries.slice().reverse().map(e=>({id:e.id,title:publicCatalog.find(r=>r.slug===e.slug)?.title??'',recipe_slug:e.slug,servings:e.nutrition.servings,nutrition:clone(e.nutrition.total),eaten_at:e.at})),entry_count:entries.length,provenance:`Демо-сессия в памяти. Снимок публичного Laravel API: ${snapshotDate}. Питание и THB — иллюстративные оценки. Цели — пример, не персональная рекомендация.`,ranking_version:'cleanup_then_time_v0.1'};
 }
}
