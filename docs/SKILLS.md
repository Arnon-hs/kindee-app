# Skills used for this release

The owner requested these global Codex installations:

```sh
npx skills add gretchenfitze/anti-slop -a codex -g
npx skills add https://evilmartians.com --skill good-readme -a codex -g
npx skills add https://evilmartians.com --skill design-system -a codex -g
```

The first installs anti-slop-audit and anti-slop-rollout. Installed global files live under ~/.agents/skills and are discovered by Codex. Existing copies were backed up before reinstall; no unrelated project was edited.

- good-readme: README now leads with what Kindee does, real platform status, screenshots, download/use steps and verified build commands. No fake metrics, badges or placeholder screenshots were added.
- design-system: existing native components and tokens were inspected and reused. The account error stays inside its native Form; business logic stays separately owned. DESIGN.md maps actual sources and clearly states that no new managed component registry was created.
- anti-slop-audit: the retained Vue client was measured with framework-aware corrections and sampled diagnostics. anti-slop/plan.md and todo.md record the findings. No raw linter total is presented as a bug count.
- anti-slop-rollout: steps 1-2 add locked detectors, reviewed entry points/direct Workbox imports and strict manual changed-file lint. Disposable Git tests demonstrate rule output and failure behavior. No automatic hooks, new CI gates or broad application cleanup were added. See QUALITY_CHECKS.md. Kotlin/Swift checks remain separate native builds/tests.

The global skill files themselves are not vendored into the app. Future Codex turns can discover the newly installed versions.
