package com.mzc.lp.domain.tenant.dto.response;

import com.mzc.lp.domain.tenant.entity.Tenant;

public record TenantDomainSettingsResponse(
        String subdomain,
        String fullSubdomainUrl,
        String customDomain,
        boolean customDomainEnabled,
        DnsInstructions dnsInstructions
) {
    public record DnsInstructions(
            String recordType,
            String recordName,
            String recordValue
    ) {}

    public static TenantDomainSettingsResponse from(Tenant tenant, String baseDomain) {
        String fullUrl = tenant.getSubdomain() + "." + baseDomain;
        boolean hasCustomDomain = tenant.getCustomDomain() != null && !tenant.getCustomDomain().isBlank();

        DnsInstructions dns = null;
        if (hasCustomDomain) {
            dns = new DnsInstructions(
                    "CNAME",
                    tenant.getCustomDomain(),
                    fullUrl
            );
        }

        return new TenantDomainSettingsResponse(
                tenant.getSubdomain(),
                fullUrl,
                tenant.getCustomDomain(),
                hasCustomDomain,
                dns
        );
    }
}
