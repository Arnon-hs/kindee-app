import {test} from 'node:test';
import assert from 'node:assert/strict';
import {isPublicRecipe} from '../src/cache-policy';
const origin='https://kindee.test';
const check=(path:string,headers:Headers=new Headers(),method='GET')=>isPublicRecipe(new URL(path,origin),{method,headers},origin);
test('only anonymous public recipe GETs are cacheable',()=>{assert.ok(check('/api/v1/recipes?market=TH'));assert.ok(check('/api/v1/recipes/tomato-eggs'));assert.equal(check('/api/v1/recipes',new Headers({Authorization:'Bearer secret'})),false);assert.equal(check('/api/v1/recipes',new Headers(),'POST'),false);});
test('private data and external origins never enter recipe cache',()=>{for(const path of ['/api/v1/auth/login','/api/v1/auth/register','/api/v1/meal-plans','/api/v1/shopping-list','/api/v1/dashboard','/api/v1/food-entries','/api/v1/weight-logs','/api/v1/recommendations','/api/v1/recipes/a/calculate','https://evil.test/api/v1/recipes'])assert.equal(check(path),false);});
