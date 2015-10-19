/// <reference path="../apimanPlugin.d.ts" />
declare module Apiman {
    var ConfigForms: {
        BASICAuthenticationPolicy: string;
        IgnoredResourcesPolicy: string;
        IPBlacklistPolicy: string;
        IPWhitelistPolicy: string;
        RateLimitingPolicy: string;
        QuotaPolicy: string;
        TransferQuotaPolicy: string;
        AuthorizationPolicy: string;
        URLRewritingPolicy: string;
        CachingPolicy: string;
    };
    var NewPolicyController: any;
}
