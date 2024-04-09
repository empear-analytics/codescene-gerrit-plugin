import "@gerritcodereview/typescript-api/gerrit";
import {ChecksFetcher} from "./fetcher";

window.Gerrit.install(async plugin => {
    const checksApi = plugin.checks();
    const fetcher = new ChecksFetcher(plugin);
    checksApi.register({fetch: changeData => fetcher.fetch(changeData) as any});
    checksApi.announceUpdate();
});