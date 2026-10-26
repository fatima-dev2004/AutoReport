import { beforeEach, describe, expect, it, vi } from 'vitest';

const mockApiClient = {
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  delete: vi.fn(),
};

vi.mock('../client.js', () => ({
  default: mockApiClient,
}));

describe('user persistence', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('returns locally stored users when the API fails', async () => {
    const storedUsers = [{ id: 1, fullName: 'Alice', email: 'alice@test.com', role: 'USER', enabled: true }];
    localStorage.setItem('autoreport_users', JSON.stringify(storedUsers));
    mockApiClient.get.mockRejectedValue(new Error('API unavailable'));

    const { listUsers } = await import('../users.js');
    const users = await listUsers();

    expect(users).toHaveLength(1);
    expect(users[0].fullName).toBe('Alice');
  });

  it('stores a created user locally when the API request fails', async () => {
    mockApiClient.post.mockRejectedValue(new Error('API unavailable'));

    const { createUser } = await import('../users.js');
    const created = await createUser({
      fullName: 'Bob',
      email: 'bob@test.com',
      password: 'secret123',
      role: 'MANAGER',
    });

    expect(created.fullName).toBe('Bob');
    expect(JSON.parse(localStorage.getItem('autoreport_users'))[0].email).toBe('bob@test.com');
  });
});
