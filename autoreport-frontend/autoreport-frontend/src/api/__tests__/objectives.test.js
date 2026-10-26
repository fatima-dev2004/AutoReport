import { beforeEach, describe, expect, it, vi } from 'vitest';

const mockApiClient = {
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  delete: vi.fn(),
  patch: vi.fn(),
};

vi.mock('../client.js', () => ({
  default: mockApiClient,
}));

describe('objective lines fallback', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('returns default production lines when backend is unavailable', async () => {
    mockApiClient.get.mockRejectedValue(new Error('API unavailable'));

    const { getProductionLines } = await import('../objectives.js');
    const lines = await getProductionLines();

    expect(lines).toEqual(expect.arrayContaining(['Ligne A', 'Ligne B', 'Ligne C', 'Ligne D']));
  });
});
