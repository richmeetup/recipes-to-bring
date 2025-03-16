import HelloWorld from '../index'

describe('getMessage()', () => {
  it('should return expected message', () => {
    expect(HelloWorld.getMessage()).toBe('Hello world!')
  })
})