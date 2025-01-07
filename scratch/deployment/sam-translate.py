import json
import logging
import sys
from functools import reduce
from pathlib import Path
import boto3
from samtranslator.model.exceptions import InvalidDocumentException
from samtranslator.public.translator import ManagedPolicyLoader
from samtranslator.translator.transform import transform
import samtranslator.parser.parser as parser
from samtranslator.yaml_helper import yaml_parse

LOG = logging.getLogger(__name__)
iam_client = boto3.client("iam")

# Hardcoded parameters
TEMPLATE_FILE = Path("../template.yaml")
OUTPUT_TEMPLATE = Path("transformed-template.json")
VERBOSE = True
STDOUT = False

if VERBOSE:
    logging.basicConfig(level=logging.DEBUG)
else:
    logging.basicConfig()

def transform_template(input_file_path: Path, output_file_path: Path, stdout: bool):
    with input_file_path.open() as f:
       sam_template = yaml_parse(f.read())

    try:
        cloud_formation_template = transform(sam_template, {}, ManagedPolicyLoader(iam_client))
        cloud_formation_template_prettified = json.dumps(cloud_formation_template, indent=1)

        if stdout:
            print(cloud_formation_template_prettified)
            return

        output_file_path.write_text(cloud_formation_template_prettified, encoding="utf-8")
        print("Wrote transformed CloudFormation template to:", output_file_path)

    except InvalidDocumentException as e:
        error_message = reduce(lambda message, error: message + " " + error.message, e.causes, e.message)
        LOG.error(error_message)

if __name__ == "__main__":
    input_file_path = TEMPLATE_FILE
    output_file_path = OUTPUT_TEMPLATE

    transform_template(input_file_path, output_file_path, STDOUT)
