import bibtexparser
from bibtexparser.bwriter import BibTexWriter
from numpy import argmin
from scholarly import scholarly
import Levenshtein
import pickle
import sys
import os



def get_bibtex_data(file):
    """Reads and parses a .bib file.

    Args:
        file (String): path to file to be parsed

    Returns:
        bib_database : Data structure to represent bibfile.
    """
    print("Read from", file)
    with open (file) as bibtexfile:
        data = bibtexparser.load(bibtexfile)
    return data

def get_titles_bib(data):
    """Extract only the titles from a bib_database.

    Args:
        data (bib_database): Bib_database to extract data from.

    Returns:
        list : list of titles
    """
    titles = []
    for elem in data.entries:
        titles.append(elem['title'].replace('\\&', "&").replace('{\\"a}', "ä").replace('{\\"o}', "ö").replace('{\\"u}', "ü").replace("{", "").replace("}", "").replace("\n", " ").replace("  ", " "))
    return titles

def get_citations(cached=False, author="Bernhard Rumpe"):
    """ This function first loads the data from 'author' from scholarly or uses cached data.
    It then produces a dictionary which assigns every given title the number of its citations. 

    Args:
        cached (bool, optional): Uses cached data instead of refreshing from scholarly. Defaults to False.
        author (str, optional): Name of the author to search publications from. Defaults to "Bernhard Rumpe".

    Returns:
        dict : The key is the title and the value is the number of its citations.
    """
    if not cached:
        print("Using Citations from Web")
        search_query = scholarly.search_author(author)
        first_author_result = next(search_query)
        author = scholarly.fill(first_author_result)
        with open('data.pickle', 'wb') as handle:
            pickle.dump(author, handle)
    else:
        print("Using Citations from Cache")
        with open('data.pickle', 'rb') as handle:
            author = pickle.load(handle)
    titles_num_cite = {}
    for elem in author['publications']:
        titles_num_cite[elem['bib']['title']] = elem['num_citations']
    return titles_num_cite

def search_citations(data, titles, titles_num_cite, filename, threshhold=0.65):
    """This function searches for every title in titles an according entry in titles_num_cite. If it finds one,
    it saves the value in the according entry in data. 

    For comparing titles, which sometimes may differ in special characters, capitalized letters etc., the
    relative Levenshtein distance is calculated (Levenshtein distance compares to length of String).
    Each title from the .bib file is compared to each title in the list, where also the citations are given.
    If the closest distance between the title from the bib and from scholar is closer than the specified threshhold,
    it is considered a match. 

    In the end, the new bibfile is saved with the same as the old name and the extension "_with_cite"

    Args:
        data (bib_database): Database from bib file.
        titles (List): List of titles to scan through.
        titles_num_cite (Dict): List of titles with citations to find matches in.
        filename (String): filename of bib file.
        threshhold (float) : Defaults to 0.65.
    """
    for title in titles:
        metrics = []
        titles_2 = []
        for title_2 in titles_num_cite.keys():
            metric = 1 - Levenshtein.distance(title, title_2)/max(len(title_2), len(title)) 
            metrics.append(metric)
            titles_2.append(title_2)
        if max(metrics) > threshhold:
            title_tmp = titles_2[argmin([-float(i) for i in metrics])]
            for i in range(len(data.entries)):
                if title == titles[i]:
                    data.entries[i]["_numcites"] = str(titles_num_cite[title_tmp])
    
    ct = 0
    for elem in data.entries:
        if '_numcites' in elem.keys():
            ct += 1
    print("Found percentage of bibtex entries in scholar: {:.2f}%".format(ct/len(data.entries)*100))
    writer = BibTexWriter()
    bibfile_name = os.path.splitext(filename)[0] + "_with_cite.bib"
    with open(bibfile_name, 'w') as bibfile:
        bibfile.write(writer.write(data))
    print("Saved in", bibfile_name)


if __name__ == "__main__":
    if len(sys.argv) >= 2:
        filename = sys.argv[1]
    else:   
        filename = "sselit.bib"
    data = get_bibtex_data(filename)
    titles = get_titles_bib(data)
    titles_num_cite = get_citations()
    search_citations(data, titles, titles_num_cite, filename)