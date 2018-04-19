from __future__ import division

import constants as const

from click import command, option
from pickle import dump
from collections import defaultdict
from tqdm import tqdm
from attrdict import AttrDict
from csv import DictReader

from utils import call_logger, file_nrows


@command()
@option('--test-file-path', required=True,
        help='Absolute path for the test file.')
@option('--train-file-path', required=True,
        help='Absolute path of the train file')
@option('--answer-pickle-path', required=True,
        help='Absolute path for the answer file.')
def main(test_file_path, train_file_path, answer_pickle_path):
    #users = get_train_users(train_file_path)
    users = [] #including all users
    user_applies, user_contacts = get_user_answers(test_file_path, users)
    #print(user_applies)
    dump_answers(user_applies, user_contacts, answer_pickle_path)


@call_logger
def get_train_users(file_path):
    users = set()
    num_rows = file_nrows(file_path)
    for row in tqdm(DictReader(open(file_path)), total=num_rows):
        app = AttrDict(**row)
        if app.kind != const.APPLY:
            continue
        users.add(app.user)
    return users


@call_logger
def get_user_answers(file_path, users):
    user_applies = defaultdict(set)
    user_contacts = defaultdict(set)
    num_rows = file_nrows(file_path)
    for row in tqdm(DictReader(open(file_path)), total=num_rows):
        app = AttrDict(**row)
        #if app.user not in users:
        #    continue
        if app.kind == const.APPLY:
            user_applies[app.user].add(app.item)
        if app.kind == const.CONTACT:
            user_contacts[app.user].add(app.item)
    return user_applies, user_contacts


def dump_answers(user_applies, user_contacts, answer_pickle_path):
    data = {"user_applies": user_applies, "user_contacts": user_contacts}
    dump(data, open(answer_pickle_path, 'w'))

if __name__ == '__main__':
    main()
