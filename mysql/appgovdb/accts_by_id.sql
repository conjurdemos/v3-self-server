SELECT appid.name, ca.resource_type, ca.resource_name, ca.username, sf.name
FROM appidentities appid, accessrequests ar, safes sf, cybraccounts ca
WHERE ar.provisioned AND NOT ar.revoked
AND ar.project_id = 4
AND appid.project_id = 4
AND appid.id = 4
AND ar.app_id = 4
AND ar.safe_id = ca.safe_id
AND sf.id = ca.safe_id ;
