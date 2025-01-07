!/bin/bash
#pip install aws-sam-translator docopt

#wget https://raw.githubusercontent.com/awslabs/serverless-application- model/develop/bin/sam-translate.py`
source ./.venv/bin/activate
pip install --upgrade pip
pip install boto3
python3 sam-translate.py --template-file=input_file.yml --output-template=output_file.json
