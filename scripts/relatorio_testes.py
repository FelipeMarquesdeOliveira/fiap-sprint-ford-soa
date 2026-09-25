#!/usr/bin/env python3
"""Gera o relatório de evidências dos testes a partir dos XML do Surefire e do CSV do JaCoCo.

Uso (após ./mvnw verify):
    python3 scripts/relatorio_testes.py            -> escreve docs/evidencias/RELATORIO-TESTES.md
    python3 scripts/relatorio_testes.py --resumo   -> imprime apenas o resumo (usado no GitHub Actions)
"""
import csv
import glob
import re
import sys
import xml.etree.ElementTree as ET
from datetime import datetime
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
SUREFIRE = RAIZ / "target" / "surefire-reports"
JACOCO = RAIZ / "target" / "site" / "jacoco" / "jacoco.csv"
SAIDA = RAIZ / "docs" / "evidencias" / "RELATORIO-TESTES.md"

CATEGORIAS = ["Sucesso (2xx)", "Erro de requisição/negócio (4xx)", "Acesso não autorizado (401/403)", "Unitário (sem HTTP)"]


def categorias_do_teste(nome):
    prefixo = nome.split(":")[0]
    codigos = [int(c) for c in re.findall(r"\b([1-5]\d{2})\b", prefixo)]
    if not codigos:
        return {CATEGORIAS[3]}, codigos
    cats = set()
    for c in codigos:
        if 200 <= c < 300:
            cats.add(CATEGORIAS[0])
        elif c in (401, 403):
            cats.add(CATEGORIAS[2])
        else:
            cats.add(CATEGORIAS[1])
    return cats, codigos


def ler_suites():
    suites = []
    for arquivo in sorted(glob.glob(str(SUREFIRE / "TEST-*.xml"))):
        raiz = ET.parse(arquivo).getroot()
        casos = []
        for caso in raiz.iter("testcase"):
            falhou = caso.find("failure") is not None or caso.find("error") is not None
            pulado = caso.find("skipped") is not None
            casos.append({
                "nome": caso.get("name"),
                "grupo": caso.get("classname"),
                "tempo": float(caso.get("time") or 0),
                "status": "FALHOU" if falhou else ("IGNORADO" if pulado else "OK"),
            })
        if casos:
            suites.append({"arquivo": Path(arquivo).stem.replace("TEST-br.com.ford.vinshare.", ""), "casos": casos})
    return suites


def cobertura():
    if not JACOCO.exists():
        return None
    linhas = list(csv.DictReader(JACOCO.open()))
    resultado = {}
    for metrica in ("LINE", "BRANCH", "INSTRUCTION", "METHOD"):
        perdidas = sum(int(l[metrica + "_MISSED"]) for l in linhas)
        cobertas = sum(int(l[metrica + "_COVERED"]) for l in linhas)
        resultado[metrica] = (cobertas, cobertas + perdidas)
    return resultado


def resumo(suites, cob):
    casos = [c for s in suites for c in s["casos"]]
    ok = sum(c["status"] == "OK" for c in casos)
    falhas = sum(c["status"] == "FALHOU" for c in casos)
    ignorados = sum(c["status"] == "IGNORADO" for c in casos)
    tempo = sum(c["tempo"] for c in casos)
    contagem = {cat: 0 for cat in CATEGORIAS}
    for c in casos:
        for cat in categorias_do_teste(c["nome"])[0]:
            contagem[cat] += 1

    saida = ["## Resumo da execução", "",
             "| Total | Aprovados | Falhas | Ignorados | Tempo |", "|---:|---:|---:|---:|---:|",
             f"| {len(casos)} | {ok} | {falhas} | {ignorados} | {tempo:.1f}s |", "",
             "### Cenários cobertos", "", "| Categoria | Testes |", "|---|---:|"]
    saida += [f"| {cat} | {n} |" for cat, n in contagem.items()]
    saida.append("")
    saida.append("> Um teste pode verificar mais de uma categoria (ex.: `GET 200 / 403 / 404`).")
    if cob:
        saida += ["", "### Cobertura de código (JaCoCo)", "", "| Métrica | Cobertura |", "|---|---:|"]
        nomes = {"LINE": "Linhas", "BRANCH": "Branches", "INSTRUCTION": "Instruções", "METHOD": "Métodos"}
        for m, (cobertas, total) in cob.items():
            saida.append(f"| {nomes[m]} | {cobertas / total * 100:.1f}% ({cobertas}/{total}) |")
    return "\n".join(saida)


def detalhes(suites):
    saida = ["## Testes por classe", "", "| Classe | Testes | Aprovados |", "|---|---:|---:|"]
    for s in suites:
        aprovados = sum(c["status"] == "OK" for c in s["casos"])
        saida.append(f"| `{s['arquivo']}` | {len(s['casos'])} | {aprovados} |")
    for s in suites:
        saida += ["", f"### {s['casos'][0]['grupo'].split(' - ')[0] if s['casos'] else s['arquivo']}", f"`{s['arquivo']}`", ""]
        grupo_atual = None
        for c in s["casos"]:
            if c["grupo"] != grupo_atual:
                grupo_atual = c["grupo"]
                saida.append(f"**{grupo_atual}**")
                saida.append("")
            saida.append(f"- [{c['status']}] {c['nome']}")
        saida.append("")
    return "\n".join(saida)


def main():
    suites = ler_suites()
    if not suites:
        sys.exit("Nenhum relatório encontrado em target/surefire-reports. Rode ./mvnw verify antes.")
    cob = cobertura()
    if "--resumo" in sys.argv:
        print(resumo(suites, cob))
        return
    conteudo = "\n".join([
        "# Relatório de testes automatizados",
        "",
        f"Gerado em {datetime.now().strftime('%d/%m/%Y %H:%M')} a partir de `target/surefire-reports` e "
        "`target/site/jacoco` com `python3 scripts/relatorio_testes.py` (após `./mvnw verify`).",
        "",
        resumo(suites, cob),
        "",
        detalhes(suites),
    ])
    SAIDA.parent.mkdir(parents=True, exist_ok=True)
    SAIDA.write_text(conteudo + "\n", encoding="utf-8")
    print(f"Relatório gerado em {SAIDA.relative_to(RAIZ)}")


if __name__ == "__main__":
    main()
