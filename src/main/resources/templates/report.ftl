<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${m.title?html}</title>
    <style>
        :root {
            --bg: #0b1220;
            --card: #121a2b;
            --muted: #a6b0c3;
            --text: #e8eefc;
            --good: #25c08a;
            --warn: #f5a524;
            --bad: #ef476f;
            --accent: #6c8cff
        }

        * {
            box-sizing: border-box
        }

        body {
            margin: 0;
            font-family: ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial;
            background: var(--bg);
            color: var(--text)
        }

        .container {
            max-width: 1200px;
            margin: 24px auto;
            padding: 0 16px
        }

        .grid {
            display: grid;
            grid-template-columns:repeat(4, 1fr);
            gap: 16px
        }

        .card {
            background: var(--card);
            border-radius: 16px;
            padding: 16px;
            box-shadow: 0 4px 16px rgba(0, 0, 0, .25)
        }

        .kpi {
            font-size: 28px;
            font-weight: 700
        }

        .muted {
            color: var(--muted);
            font-size: 13px
        }

        .pill {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 999px;
            font-size: 12px
        }

        .pill.good {
            background: rgba(37, 192, 138, .15);
            color: var(--good)
        }

        .pill.warn {
            background: rgba(245, 165, 36, .15);
            color: var(--warn)
        }

        .pill.bad {
            background: rgba(239, 71, 111, .18);
            color: var(--bad)
        }

        h1 {
            margin: 8px 0 4px 0;
            font-size: 28px
        }

        h2 {
            margin: 16px 0 8px 0;
            font-size: 20px
        }

        table {
            width: 100%;
            border-collapse: collapse
        }

        th, td {
            padding: 10px;
            border-bottom: 1px solid rgba(255, 255, 255, .06);
            vertical-align: top
        }

        tr:hover {
            background: rgba(255, 255, 255, .03)
        }

        code {
            background: rgba(255, 255, 255, .08);
            padding: 2px 6px;
            border-radius: 6px
        }

        .small {
            font-size: 12px
        }

        details {
            background: rgba(255, 255, 255, .03);
            padding: 8px;
            border-radius: 8px
        }

        summary {
            cursor: pointer
        }

        .footer {
            color: #93a1bd;
            font-size: 12px;
            margin-top: 24px
        }

        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 12px;
            flex-wrap: wrap
        }

        .badge {
            background: rgba(108, 140, 255, .15);
            color: var(--accent);
            padding: 6px 10px;
            border-radius: 999px;
            font-size: 12px
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <div>
            <div class="badge">${m.app?html}</div>
            <h1>${m.title?html}</h1>
            <div class="muted">Run: ${m.runId?html} • Generated: ${m.generatedAt?html}</div>
        </div>
    </div>
    <div class="grid" style="margin-top:16px;">
        <div class="card">
            <div class="muted">Total heal attempts</div>
            <div class="kpi">${m.total}</div>
        </div>
        <div class="card">
            <div class="muted">Heals used</div>
            <div class="kpi" style="color:var(--good)">${m.used}</div>
        </div>
        <div class="card">
            <div class="muted">Failed</div>
            <div class="kpi" style="color:var(--bad)">${m.failed}</div>
        </div>
        <div class="card">
            <div class="muted">Avg confidence</div>
            <div class="kpi">${(m.avgConfidence*100)?string["0.0"]}%</div>
        </div>
    </div>
    <div class="card" style="margin-top:16px;">
        <h2>Healed Locators</h2>
        <table>
            <thead>
            <tr>
                <th>Test</th>
                <th>Page</th>
                <th>Original</th>
                <th>Healed</th>
                <th>Conf.</th>
                <th>Status</th>
                <th>Duration</th>
            </tr>
            </thead>
            <tbody>
            <#list m.events as e>
                <tr>
                    <td>${e.testCase?html}<br/><span class="small muted">${e.timestamp!""}</span></td>
                    <td>${e.page?html}</td>
                    <td><code>${e.originalLocator?html}</code></td>
                    <td>
                        <code>${e.healedLocator?html}</code>
                        <#if e.domSnippet?? && e.domSnippet?length gt 0>
                            <details style="margin-top:6px;">
                                <summary class="small">context</summary>
                                <pre class="small" style="white-space:pre-wrap;">${e.domSnippet?html}</pre>
                            </details>
                        </#if>
                    </td>
                    <td>${(e.confidence*100)?string["0.0"]}%</td>
                    <td><#if e.status?upper_case == "USED"><span
                                class="pill good">USED</span><#elseif e.status?upper_case == "FAILED"><span
                                class="pill bad">FAILED</span><#else><span
                                class="pill warn">${e.status?html}</span></#if>
                        <#if e.reason?? && e.reason?length gt 0>
                            <div class="small muted">${e.reason?html}</div></#if>
                    </td>
                    <td>${e.durationMs} ms</td>
                </tr>
            </#list>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
