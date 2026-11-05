export interface ProfileInput {market:string;timezone:string;calories:number;protein_g:number;fat_g:number;carbs_g:number}
export const defaultProfile:ProfileInput={market:'TH',timezone:'Asia/Bangkok',calories:2000,protein_g:100,fat_g:65,carbs_g:250};
export function validateProfile(input:ProfileInput):ProfileInput {
 if(!['TH','US'].includes(input.market))throw new Error('invalidProfile');
 try{new Intl.DateTimeFormat('en',{timeZone:input.timezone});}catch{throw new Error('invalidProfile');}
 for(const key of ['calories','protein_g','fat_g','carbs_g'] as const){const n=input[key];if(!Number.isInteger(n)||n<(key==='calories'?1:0)||n>(key==='calories'?10000:1000))throw new Error('invalidProfile');}
 return {...input};
}
