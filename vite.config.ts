import {defineConfig} from 'vite';
import vue from '@vitejs/plugin-vue';
import {VitePWA} from 'vite-plugin-pwa';
const publicDemo=process.env.KINDEE_PUBLIC_DEMO==='true';
const native=process.env.KINDEE_NATIVE==='true';
const base=publicDemo?'/app/':'/';
export default defineConfig({base,resolve:{alias:native?{'virtual:pwa-register/vue':new URL('./src/native-pwa.ts',import.meta.url).pathname}:{}},define:{'import.meta.env.VITE_NATIVE':JSON.stringify(native?'true':'false'),'import.meta.env.VITE_LOCAL_API':JSON.stringify(process.env.KINDEE_LOCAL_API==='true'?'true':'false'),'import.meta.env.VITE_PUBLIC_DEMO':JSON.stringify(publicDemo?'true':'false')},plugins:[vue(),VitePWA({disable:native,
 strategies:'injectManifest',srcDir:'src',filename:'sw.ts',registerType:'prompt',injectRegister:false,
 includeAssets:['offline.html','icons/*.png'],
 injectManifest:{globPatterns:['**/*.{js,css,html,png,svg,ico,webp}']},
 manifest:{id:base,name:'KinDee — Eat well. Live easy.',short_name:'KinDee',description:'Питание, рецепты и меньше хлопот. Thailand first.',lang:'ru',start_url:base,scope:base,display:'standalone',background_color:'#f7f8f2',theme_color:'#163d2e',orientation:'any',icons:[{src:base+'icons/icon-192.png',sizes:'192x192',type:'image/png',purpose:'any'},{src:base+'icons/icon-512.png',sizes:'512x512',type:'image/png',purpose:'any'},{src:base+'icons/maskable-512.png',sizes:'512x512',type:'image/png',purpose:'maskable'}],shortcuts:[{name:'Рецепты',url:base+(publicDemo?'#/recipes':'recipes')},{name:'Мой прогресс',url:base+(publicDemo?'#/progress':'progress')}]}
})],server:{host:'0.0.0.0',proxy:{'/api':'http://localhost:8080'}}});
