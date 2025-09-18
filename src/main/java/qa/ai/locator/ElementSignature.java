package qa.ai.locator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openqa.selenium.*;
import java.util.*;

public class ElementSignature {
    public String key;            // e.g., "login.username.input"
    public String text;
    public String tag;
    public Map<String,String> attrs = new HashMap<>();
    public String ariaRole;
    public String neighborTextLeft;
    public String neighborTextAbove;
    public List<String> classes = new ArrayList<>();
    public String pathCss;        // last known stable css
    public String pathXpath;      // last known stable xpath
    public String url;

    public static ElementSignature from(WebDriver driver, WebElement el, String key) {
        ElementSignature s = new ElementSignature();
        s.key = key;
        s.url = driver.getCurrentUrl();
        s.tag = el.getTagName();
        s.text = safe(el::getText);
        s.attrs.put("id", getAttr(el,"id"));
        s.attrs.put("name", getAttr(el,"name"));
        s.attrs.put("data-testid", getAttr(el,"data-testid"));
        s.attrs.put("placeholder", getAttr(el,"placeholder"));
        s.attrs.put("type", getAttr(el,"type"));
        s.attrs.put("value", getAttr(el,"value"));
        s.classes = classes(el);
        s.ariaRole = getAttr(el,"role");

        // store current best selectors (optional)
        s.pathCss = (String)((JavascriptExecutor)driver).executeScript(JS_GET_CSS, el);
        s.pathXpath = (String)((JavascriptExecutor)driver).executeScript(JS_GET_XPATH, el);
        // neighbors (rough, optional)
        s.neighborTextLeft = (String)((JavascriptExecutor)driver).executeScript(JS_LEFT_NEIGHBOR, el);
        s.neighborTextAbove = (String)((JavascriptExecutor)driver).executeScript(JS_ABOVE_NEIGHBOR, el);
        return s;
    }

    private static String getAttr(WebElement el, String name) {
        try { String v = el.getAttribute(name); return v==null? "": v; } catch(Exception e){ return ""; }
    }
    private static String safe(java.util.concurrent.Callable<String> c){
        try { return Optional.ofNullable(c.call()).orElse(""); } catch(Exception e){ return ""; }
    }
    private static java.util.List<String> classes(WebElement el) {
        String cls = getAttr(el,"class");
        if (cls==null || cls.isBlank()) return List.of();
        return Arrays.stream(cls.split("\\s+")).distinct().toList();
    }

    @JsonIgnore
    private static final String JS_GET_CSS = """
      function cssPath(el){
        if (!(el instanceof Element)) return '';
        const path=[];
        while (el.nodeType===Node.ELEMENT_NODE){
          let selector = el.nodeName.toLowerCase();
          if (el.id){ selector += '#'+el.id; path.unshift(selector); break; }
          else {
            let sib=el, nth=1;
            while (sib=sib.previousElementSibling){ if (sib.nodeName===el.nodeName) nth++; }
            selector += ':nth-of-type('+nth+')';
          }
          path.unshift(selector); el=el.parentNode;
        }
        return path.join(' > ');
      }
      return cssPath(arguments[0]);
    """;

    @JsonIgnore
    private static final String JS_GET_XPATH = """
      function getXPath(el){
        if (el.id) return '//*[@id="'+el.id+'"]';
        const parts=[];
        while (el && el.nodeType===Node.ELEMENT_NODE){
          let ix=0, sib=el.previousSibling;
          while (sib){ if (sib.nodeType===Node.ELEMENT_NODE && sib.nodeName===el.nodeName) ix++; sib=sib.previousSibling; }
          const tag=el.nodeName.toLowerCase();
          const seg=(ix? tag+'['+(ix+1)+']' : tag);
          parts.unshift(seg); el=el.parentNode;
        }
        return '/'+parts.join('/');
      }
      return getXPath(arguments[0]);
    """;

    @JsonIgnore
    private static final String JS_LEFT_NEIGHBOR = """
      const r=arguments[0].getBoundingClientRect();
      const x=r.left-5, y=r.top + r.height/2;
      const el=document.elementFromPoint(Math.max(0,x), Math.max(0,y));
      return el && el.innerText ? el.innerText.trim().slice(0,120) : '';
    """;
    @JsonIgnore
    private static final String JS_ABOVE_NEIGHBOR = """
      const r=arguments[0].getBoundingClientRect();
      const x=r.left + r.width/2, y=r.top - 5;
      const el=document.elementFromPoint(Math.max(0,x), Math.max(0,y));
      return el && el.innerText ? el.innerText.trim().slice(0,120) : '';
    """;
}
