import catalog from './data/public-recipes.json';
import type {Nutrition} from './api/recipes';
export interface PlanInput {recipe_slug:string;servings:number;planned_for:string}
export interface PlanIngredient {ingredient_id:string|number;name:string;preparation_state:string;grams:number;allergens:string[]|null}
export interface PlanEntry extends PlanInput {id:number;title:string;market:string;nutrition:Nutrition;ingredients:PlanIngredient[];provenance:string}
export interface ShoppingList {from:string;to:string;items:PlanIngredient[];source?:string}
export function validDate(value:string):boolean{return /^\d{4}-\d{2}-\d{2}$/.test(value)&&Number.isFinite(Date.parse(value+'T00:00:00Z'))&&new Date(value+'T00:00:00Z').toISOString().slice(0,10)===value;}
export function weekEnd(value:string):string{if(!validDate(value))throw new Error('planRangeError');const day=new Date(value+'T00:00:00Z');day.setUTCDate(day.getUTCDate()+6);return day.toISOString().slice(0,10);}
export function validRange(from:string,to:string):void{if(!validDate(from)||!validDate(to)||to<from||(Date.parse(to)-Date.parse(from))/86400000>30)throw new Error('planRangeError');}
export class DemoPlanner {
 private entries:PlanEntry[]=[];private nextId=1;
 constructor(private readonly calculate:(slug:string,servings:number)=>Nutrition){}
 clear(){this.entries=[];this.nextId=1;}
 add(input:PlanInput,market:string):PlanEntry{
  const recipe=catalog.recipes.find(r=>r.slug===input.recipe_slug&&r.market===market);
  if(!recipe||!Number.isInteger(input.servings)||input.servings<1||input.servings>20||!validDate(input.planned_for)||input.planned_for<'2000-01-01'||input.planned_for>='2100-01-01')throw new Error('planInvalid');
  const entry:PlanEntry={...input,id:this.nextId++,title:recipe.title,market,nutrition:this.calculate(input.recipe_slug,input.servings),ingredients:recipe.ingredients.map(i=>({ingredient_id:i.name,name:i.name,preparation_state:i.preparation_state,grams:Math.round(i.grams*input.servings/recipe.servings*100)/100,allergens:i.allergens})),provenance:'illustrative_seed_snapshot'};
  this.entries.push(structuredClone(entry));return structuredClone(entry);
 }
 remove(id:number){if(!this.entries.some(x=>x.id===id))throw new Error('entryNotFound');this.entries=this.entries.filter(x=>x.id!==id);}
 list(from:string,to:string):PlanEntry[]{validRange(from,to);return structuredClone(this.entries.filter(x=>x.planned_for>=from&&x.planned_for<=to).sort((a,b)=>a.planned_for.localeCompare(b.planned_for)||a.id-b.id));}
 shopping(from:string,to:string):ShoppingList{
  const items=new Map<string,PlanIngredient>();
  for(const entry of this.list(from,to))for(const ingredient of entry.ingredients){const key=ingredient.ingredient_id+'|'+ingredient.preparation_state;const previous=items.get(key);items.set(key,{...ingredient,grams:Math.round(((previous?.grams??0)+ingredient.grams)*100)/100});}
  return {from,to,items:[...items.values()].sort((a,b)=>a.name.localeCompare(b.name))};
 }
}
