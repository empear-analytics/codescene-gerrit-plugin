import {PluginApi} from '@gerritcodereview/typescript-api/plugin';
import {ChangeData, CheckResult, FetchResponse, ResponseCode} from "@gerritcodereview/typescript-api/checks";

type UpdateFn = (x: CheckResult) => CheckResult;
export class ChecksFetcher {
    plugin: PluginApi;

    changeData: ChangeData | null;

    constructor(plugin: PluginApi) {
        this.plugin = plugin;
        this.changeData = null;
    }

    private updateResults(resp: FetchResponse, f: UpdateFn): FetchResponse {
        if (resp.responseCode == ResponseCode.OK && resp.runs) {
            const run = resp.runs[0];
            const results = (run.results || []).map(f);
            return {...resp, runs: [{...run, results: results}]};
        } else {
            return resp;
        }
    }

    private getUrl(): string {
        if (this.changeData) {
            const {patchsetSha, changeInfo} = this.changeData;
            return `/changes/${changeInfo.id}/revisions/${patchsetSha}/${this.plugin.getPluginName()}~result`;
        } else {
            console.error("Invalid state, no change data");
            return "";
        }
    }

    private async rerun()  {
        const resp: any = await this.plugin
            .restApi()
            .put(this.getUrl());
        if (resp.code == 0) {
            // CodeScene not configured
            console.info("CodeScene plugin is not configured");
            return {message: "CodeScene plugin is not configured"};
        } else if (resp.code == 400) {
            console.info(resp.error);
            return {message: resp.error};
        }
        if (resp.code == 500) {
            console.error(resp.error);
            return {message: resp.error};
        }
        return {};
    }

    private addSuppressionAction(x: CheckResult): CheckResult {
        if (x.externalId) {
            if (x.externalId == "RERUN") {
                return {
                    ...x, actions: [{
                        name: "Run Again", primary: true, callback:
                            (_change: number,
                             _patchset: number,
                             _attempt: number | undefined,
                             _externalId: string | undefined,
                             _checkName: string | undefined,
                             _actionName: string) => {
                              return this.rerun();
                            }
                    }]
                }
            } else {
                return {
                    ...x, actions: [{
                        name: "Suppress", primary: true, callback:
                            (_change: number,
                             _patchset: number,
                             _attempt: number | undefined,
                             _externalId: string | undefined,
                             _checkName: string | undefined,
                             _actionName: string) => {
                              window.open(x.externalId, '_blank');
                              return Promise.resolve({});

                            }
                    }]
                }
            }
        } else {
            return x;
        }
    }

    async fetch(changeData: ChangeData) {
        this.changeData = changeData;
        const resp: any = await this.plugin
            .restApi()
            .get(this.getUrl());
        if (resp.code == 200) {
            const ret = JSON.parse(resp.body);
            return this.updateResults(ret, (x: CheckResult) => this.addSuppressionAction(x));
        } else if (resp.code == 0) {
            // CodeScene not configured
            console.info("CodeScene plugin is not configured");
            return {responseCode: ResponseCode.OK};
        } else if (resp.code == 400) {
            console.info(resp.error);
            return {responseCode: ResponseCode.OK};
        } if (resp.code == 500) {
            return {responseCode: ResponseCode.ERROR, errorMessage: resp.error};
        }
        console.warn("Invalid state" + resp);
        return {responseCode: ResponseCode.OK};
    }

}
