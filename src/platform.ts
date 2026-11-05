/** Platform boundary for a future Capacitor shell; never put credentials into SW messages. */
export interface NativeCapabilities {
 secureTokenStorage: boolean;
 systemWidgets: boolean;
 deepLinks: boolean;
}
export interface WidgetProjection {
 widget:'calories'|'recommendations'|'meal-prep'|'weight';
 date:string;
 timezone:string;
 value:unknown;
}
export interface NativeBridge {
 capabilities:NativeCapabilities;
 publishWidget(projection:WidgetProjection):Promise<void>;
 clearPrivateState():Promise<void>;
}
export const browserCapabilities:NativeCapabilities={secureTokenStorage:false,systemWidgets:false,deepLinks:false};
