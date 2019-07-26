
if hasPath() then
  if finishedPathStep() then
    getNextPathStep()
    if not hasPath() then
      return
    end
  end
  pos = getPosition()
  stepPos = getCurrentPathStep()
  dir = getNormalizedDifference(pos, stepPos)
  lookInDirection(dir)
  dt = delta()
  translate(dir.x*40*dt, dir.y*40*dt, dir.z*40*dt)
end
