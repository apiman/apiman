import { ClientMappingImpl } from './client-mapping-impl';

describe('ClientMappingImpl', () => {
  it('should create an instance', () => {
    expect(new ClientMappingImpl('clientId', 'organizationId')).toBeTruthy();
  });
});
