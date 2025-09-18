# 🧭 AI Locator Healing for Selenium (Java)

**Self-healing locators** for UI tests: drop-in `By` wrapper that learns robust element “signatures”, generates alternative selectors when the primary one breaks, and recovers **without** changing your test code.

---

## ✨ What it does
- 🧠 Learns element signatures (tag, text, attrs, role, classes, neighbors, last-known CSS/XPath, URL).
- 🧩 Generates locator candidates from the live DOM (id/name/data-testid/label/role/text/nth-of-type…).
- 🎯 Ranks candidates with heuristics (stability > uniqueness > similarity) and picks a unique match.
- 💾 Persists signatures to a local JSON store for cross-run memory.
- 🤖 Optional LLM reranker (Ollama: `llama3`, `mistral`, etc.) to re-score tough matches.
- 🧬 Works with Selenium 4+ (Java).

---

## 🛠️ Quick Start

### 1) Use it in tests
```java
WebDriver driver = new ChromeDriver();
SignatureStore store = new SignatureStore(Path.of("signatures.json"));

By username = new SelfHealingBy(By.id("username"), "login.username.input", store);
By password = new SelfHealingBy(By.name("password"), "login.password.input", store);
By submit   = new SelfHealingBy(By.cssSelector("[data-testid='login-btn']"), "login.submit.button", store);

driver.get("https://example.com/login");
driver.findElement(username).sendKeys("usernamehere");
driver.findElement(password).sendKeys("secretpasswordhere");
driver.findElement(submit).click();
```

---

## 📁 Project Structure (reference)
```
src/main/java/qa/ai/locator/
  ├─ SelfHealingBy.java
  ├─ LocatorCandidate.java
  ├─ HeuristicScorer.java
  ├─ DomSnapshot.java
  ├─ ElementSignature.java
  ├─ SignatureStore.java
  ├─ LlmClient.java
  └─ LocatorHelper.java
```

---

## ⚙️ How it works
1. Primary attempt → Use your `By`. If found, persist signature.  
2. On failure → Snapshot DOM, generate candidates, score & rerank.  
3. Heal & continue → Use best match, update signature, continue test.

---

## ⚡ Optional: LLM Reranker (Ollama)
```bash
ollama pull mistral
```

Configure seam:
```java
LlmClient llm = new LlmClient("http://127.0.0.1:11434", "mistral");
double boost = llm.rerank(signatureJson, candidateSelector);
```

---

## 🧪 Tips
- Prefer stable attributes (`data-testid`, `aria-*`, `id`).
- Use explicit waits.  
- Log heals for team awareness.

---

## 🗺️ Roadmap
- [ ] Visibility/interactability filters  
- [ ] Proximity scoring (label/neighbor text)  
- [ ] Telemetry (JSON log of heals)  
- [ ] Page Object auto-suggestions  
- [ ] Playwright (JS/TS) sibling package

---

## 🤝 Contributing
```bash
git checkout -b feat/my-enhancement
git add .
git commit -m "feat: add self-heal improvement"
git push origin feat/my-enhancement
```

Then open a Pull Request 🚀

---

## 📜 License
MIT — free for commercial and open-source use.
