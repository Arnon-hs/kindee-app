import manifest from './data/food-photos.json';
export const photos=manifest.photos;
const recipePhotos:Record<string,string>={'chicken-rice-bowl':'chicken-rice','garlic-shrimp-rice':'shrimp-rice','tomato-eggs':'tomato-eggs','tofu-spinach-bowl':'tofu-vegetables'};
export function photoFor(slug:string){return photos.find(p=>p.id===(recipePhotos[slug]??slug));}
export function photoUrl(filename:string):string{return import.meta.env.BASE_URL+'food/'+filename;}
