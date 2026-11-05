import {registerPlugin} from '@capacitor/core';
import type {HealthDaily} from './health-types';
export interface HealthBridge {
 availability():Promise<{status:'available'|'unavailable';provider:string}>;
 authorize():Promise<{status:'authorized'|'denied'|'requested'}>;
 sources(options:{date:string;timezone:string}):Promise<{origins:string[]}>;
 readDaily(options:{date:string;timezone:string;origin:string}):Promise<HealthDaily>;
 readWearDaily(options:{date:string;timezone:string}):Promise<HealthDaily>;
 clearSession():Promise<void>;
 disconnect():Promise<void>;
}
export const NativeHealth=registerPlugin<HealthBridge>('KinDeeHealth');
