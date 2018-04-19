from tqdm import tqdm
cr = open('/Users/fzafari/librec-latest/core/Recommendations.txt', 'r')

from collections import defaultdict

recs = defaultdict(list)
for row in tqdm(cr):
    rows = row.replace("\n","").split(" ")
    recs[rows[0]].append((rows[1], rows[2]))

#print(recs)

from pickle import dump, load

dump(recs.copy(), open("/Users/fzafari/librec-latest/core/sg.pickle", 'w'))

recs_loaded = load(open("/Users/fzafari/librec-latest/core/sg.pickle"))

#print(recs_loaded)
