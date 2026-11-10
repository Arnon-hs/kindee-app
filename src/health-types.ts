export interface HealthDaily {
 provider:'health_connect'|'healthkit'|'wear_os'; origin:string;
 active_calories_kcal:number|null; total_energy_kcal:number|null; steps:number|null; collected_at:string;
}
export interface HealthSummary {
 date:string; timezone:string; status:'available'|'stale'|'no_data';
 sources:{provider:string;origin:string;last_date:string;selected:boolean}[];
 daily:HealthDaily|null; provenance:'client_reported'; activity_goal_adjustment_kcal:number;
}
export interface OnboardingInput {
 goal_kind:'track'|'maintain'|'lose'|'gain'; height_cm:number|null; baseline_kg:number|null; target_kg:number|null;
 activity_level:'low'|'moderate'|'high'; health_preference:'health_connect'|'healthkit'|'wear_os'|'unsupported'|'later'; consent:boolean;
}
/** Weight change comes from measurements; wearable calories cannot measure lost kilograms. */
export function measuredWeightChange(logs:{kg:string|number}[]):number|null {
 const first=logs[0],last=logs.at(-1);
 if(logs.length<2||first===undefined||last===undefined)return null;
 return Math.round((Number(last.kg)-Number(first.kg))*100)/100;
}
