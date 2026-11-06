import {test} from 'node:test';
import assert from 'node:assert/strict';
import {DemoSession} from '../src/demo-session';
import {validDate,validRange,weekEnd} from '../src/planning';

test('saved demo plans scale and aggregate shopping without adding consumed calories',()=>{
 const session=new DemoSession(()=>new Date('2026-11-06T10:00:00Z'));
 const one=session.planner.add({recipe_slug:'chicken-rice-bowl',servings:3,planned_for:'2026-11-08'},'TH');
 session.planner.add({recipe_slug:'chicken-rice-bowl',servings:1,planned_for:'2026-11-08'},'TH');
 one.ingredients[0].grams=1;
 const list=session.planner.shopping('2026-11-06','2026-11-12');
 assert.equal(list.items.find(x=>x.name==='Chicken breast raw')!.grams,720);
 assert.equal(session.dashboard().entry_count,0);assert.equal(session.dashboard().meal_prep.saved_plan,true);
 session.clear();assert.equal(session.planner.list('2026-11-06','2026-11-12').length,0);
});
test('planner rejects invalid dates, excessive portions and foreign-market recipes and isolates sessions',()=>{
 assert.equal(validDate('2026-02-31'),false);assert.equal(weekEnd('2026-12-29'),'2027-01-04');
 assert.throws(()=>validRange('2026-11-06','2027-11-06'));
 const a=new DemoSession(),b=new DemoSession();
 for(const patch of [{servings:0},{servings:21},{servings:1.5},{planned_for:'invalid'}])assert.throws(()=>a.planner.add({recipe_slug:'chicken-rice-bowl',servings:3,planned_for:'2026-11-08',...patch},'TH'));
 assert.throws(()=>a.planner.add({recipe_slug:'chicken-rice-bowl',servings:3,planned_for:'2026-11-08'},'US'));
 const entry=a.planner.add({recipe_slug:'chicken-rice-bowl',servings:3,planned_for:'2026-11-08'},'TH');
 assert.equal(b.planner.list('2026-11-06','2026-11-12').length,0);
 a.planner.remove(entry.id);assert.equal(a.planner.shopping('2026-11-06','2026-11-12').items.length,0);
});
