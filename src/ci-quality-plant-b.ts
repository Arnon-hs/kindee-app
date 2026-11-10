export function qualityBeta(input:number):number {
 let total=0;
 for(let index=0;index<10;index++) {
  if(input>index) {
   total += input*index+7;
  } else {
   total -= input-index-3;
  }
 }
 return Math.round(total*100)/100;
}
