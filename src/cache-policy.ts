export function isPublicRecipe(url: URL, request: Pick<Request,'method'|'headers'>, origin:string):boolean {
 return url.origin===origin && request.method==='GET' && !request.headers.has('Authorization') && /^\/api\/v1\/recipes(?:\/[a-z0-9-]+)?$/.test(url.pathname);
}
