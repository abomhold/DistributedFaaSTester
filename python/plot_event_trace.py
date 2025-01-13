import matplotlib.pyplot as plt
import numpy as np
import pandas as pd


def create_event_plot(df: pd.DataFrame):
    fig, (ax1, ax2) = plt.subplots(nrows=2, figsize=(30, 15))
    ax1.plot(df['expected invocation time'], label='Expected', marker='o')
    ax1.plot(df['actual invocation time'], label='Actual', marker='o')
    ax1.set_ylabel('Invocation Time (ms)')
    ax1.set_title('Actual vs. Expected Invocation Times')
    ax1.legend()

    ax2.plot(df['drift'], label='Drift', marker='o', color='purple')
    ax2.set_xlabel('Event Index')
    ax2.set_ylabel('Drift (ms)')
    ax2.set_title('Drift (Actual - Expected)')
    ax2.legend()
    plt.savefig(f'./event.png')
    plt.show()


def create_hist_plot(df: pd.DataFrame):
    fig, ax = plt.subplots()
    bins = np.histogram_bin_edges(df['drift'], bins='auto')
    ax.hist(df['drift'], bins=bins)
    ax.set_xlabel('Drift (ms)')
    ax.set_ylabel('Count')
    ax.set_title('Histogram of Drift')
    plt.savefig(f'./hist.png')
    plt.show()


def main():
    df = pd.read_csv('eventTraceExperimentResults.csv')
    df.dropna(subset=['expected invocation time', 'actual invocation time'], inplace=True)
    df['expected invocation time'] = pd.to_numeric(df['expected invocation time'], errors='coerce')
    df['actual invocation time'] = pd.to_numeric(df['actual invocation time'], errors='coerce')
    df['drift'] = df['actual invocation time'] - df['expected invocation time']

    create_event_plot(df)
    create_hist_plot(df)


if __name__ == "__main__":
    main()
