"""Prints the frame metrics from a macrobenchmark JSON file as a small table."""
import json
import sys

path = sys.argv[1]
with open(path) as f:
    data = json.load(f)

for bench in data["benchmarks"]:
    print(f"{bench['className'].split('.')[-1]}.{bench['name']}  ({path})")
    for name, series in bench.get("sampledMetrics", {}).items():
        print(f"  {name:<22} P50 {series['P50']:8.1f}   P90 {series['P90']:8.1f}   P95 {series['P95']:8.1f}   P99 {series['P99']:8.1f}")
    for name, metric in bench.get("metrics", {}).items():
        print(f"  {name:<22} median {metric['median']:8.1f}")
