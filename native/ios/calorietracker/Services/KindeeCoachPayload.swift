import Foundation

enum KindeeCoachPayload {
    static func isServerEndpoint(_ value:String, server:String) -> Bool {
        guard let a=URLComponents(string:value),let b=URLComponents(string:server),
              a.scheme=="https",b.scheme=="https",a.user==nil,a.password==nil,a.query==nil,a.fragment==nil,
              b.user==nil,b.password==nil,b.query==nil,b.fragment==nil else { return false }
        return a.host?.lowercased()==b.host?.lowercased() && a.port==b.port && b.path=="/api/v1" && a.path=="/api/v1/ai"
    }
    static func bounded(_ text:String, bytes:Int) -> String { String(decoding:text.utf8.prefix(bytes),as:UTF8.self) }
    static func messages(system:String, history:[(String,String)], question:String) -> [[String:Any]] {
        let instructions="\nKINDEE SERVER MODE: Answer only from this bounded summary. No history tools or proposal cards are available. Do not claim to modify goals, plans, orders or activity. If details are unavailable, say so."
        var messages:[[String:Any]]=[["role":"system","content":bounded(system,bytes:10000)+instructions]]
        for (role,text) in history.suffix(6) where role=="user" || role=="assistant" {
            messages.append(["role":role,"content":bounded(text,bytes:1000)])
        }
        messages.append(["role":"user","content":bounded(question,bytes:4000)])
        return messages
    }
}
