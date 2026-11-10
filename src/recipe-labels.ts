type UiLocale='ru'|'en'|'th';
const recipes:Record<string,Record<UiLocale,[string,string]>>={
 'chicken-rice-bowl':{ru:['Боул с курицей и рисом','Белковый боул с овощами'],en:['Chicken Rice Bowl','High-protein bowl with vegetables'],th:['ข้าวหน้าไก่โบวล์','ข้าวหน้าไก่และผัก โปรตีนสูง']},
 'garlic-shrimp-rice':{ru:['Креветки с чесноком и рисом','Быстрое блюдо с креветками'],en:['Garlic Shrimp Rice','Fast shrimp with fragrant rice'],th:['ข้าวกุ้งกระเทียม','กุ้งกับข้าวหอม ทำได้เร็ว']},
 'tomato-eggs':{ru:['Яйца с томатами','Простое блюдо на одной сковороде'],en:['Tomato Eggs','Simple one-pan eggs in tomato sauce'],th:['ไข่ผัดมะเขือเทศ','เมนูง่ายในกระทะเดียว']},
 'tofu-spinach-bowl':{ru:['Боул с тофу и шпинатом','Тофу и зелень с небольшим бюджетом'],en:['Tofu Spinach Bowl','Budget-friendly tofu and greens'],th:['โบวล์เต้าหู้ผักโขม','เต้าหู้กับผักในงบประหยัด']}
};
const ingredients:Record<string,[string,string]>={
 'Chicken breast raw':['Куриная грудка, сырая','อกไก่ดิบ'],'Jasmine rice dry':['Рис жасмин, сухой','ข้าวหอมมะลิสาร'],'Broccoli raw':['Брокколи, сырая','บรอกโคลีดิบ'],'Shrimp raw':['Креветки, сырые','กุ้งดิบ'],'Garlic raw':['Чеснок, сырой','กระเทียมดิบ'],'Olive oil':['Оливковое масло','น้ำมันมะกอก'],'Egg whole raw':['Яйцо, сырое','ไข่ดิบ'],'Tomato raw':['Томат, сырой','มะเขือเทศดิบ'],'Firm tofu':['Твердый тофу','เต้าหู้แข็ง'],'Spinach raw':['Шпинат, сырой','ผักโขมดิบ']
};
export function recipeLabel(slug:string,locale:string,fallback:string,description=false):string{return recipes[slug]?.[locale as UiLocale]?.[description?1:0]??fallback;}
export function ingredientLabel(name:string,locale:string):string{
 if(locale==='ru')return ingredients[name]?.[0]??name;
 if(locale==='th')return ingredients[name]?.[1]??name;
 return name;
}
export function scaleIngredients<T extends {grams:number}>(ingredients:T[],base:number,requested:number):T[]{if(!Number.isInteger(requested)||requested<1||requested>30||base<1)throw new Error('invalidServings');return ingredients.map(i=>({...i,grams:Math.round(i.grams/base*requested*100)/100}));}
