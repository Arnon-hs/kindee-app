import {test} from 'node:test';
import assert from 'node:assert/strict';
import {measuredWeightChange} from '../src/health-types';
import {isPublicRecipe} from '../src/cache-policy';
import {healthMessages} from '../src/health-messages';
test('weight change requires real measurements and preserves gain/loss sign',()=>{
 assert.equal(measuredWeightChange([]),null);assert.equal(measuredWeightChange([{kg:80}]),null);
 assert.equal(measuredWeightChange([{kg:'80.10'},{kg:'79.85'}]),-.25);assert.equal(measuredWeightChange([{kg:65},{kg:66.2}]),1.2);
});
test('onboarding and health routes can never enter the recipe cache',()=>{
 for(const path of ['/api/v1/health','/api/v1/health/sync','/api/v1/onboarding'])assert.equal(isPublicRecipe(new URL(path,'https://kindee.test'),{method:'GET',headers:new Headers()},'https://kindee.test'),false);
});
test('health and onboarding translations cover every language with matching parameters',()=>{
 for(const dictionary of Object.values(healthMessages)){
  assert.deepEqual(Object.keys(dictionary),Object.keys(healthMessages.ru));
  for(const [key,text] of Object.entries(dictionary))assert.deepEqual(text.match(/\{[^}]+\}/g),healthMessages.ru[key as keyof typeof healthMessages.ru].match(/\{[^}]+\}/g));
 }
});
